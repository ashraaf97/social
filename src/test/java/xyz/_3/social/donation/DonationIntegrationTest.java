package xyz._3.social.donation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import xyz._3.social.model.Donation;
import xyz._3.social.model.DonationStatus;
import xyz._3.social.model.OverlayEvent;
import xyz._3.social.model.TtsStatus;
import xyz._3.social.repository.DonationRepository;
import xyz._3.social.repository.OverlayEventRepository;
import xyz._3.social.service.AiReaderService;
import xyz._3.social.service.DonationService;

@SpringBootTest
@Transactional
@DisplayName("Donation logic integration tests")
class DonationIntegrationTest {

    private static final String STREAMER_ID = "streamer-it";
    private static final String DONATIONS_ENDPOINT = "/api/v1/donations";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DonationService donationService;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private OverlayEventRepository overlayEventRepository;

    @MockitoBean
    private AiReaderService aiReaderService;

    @MockitoBean
    private ElevenLabsTextToSpeechModel elevenLabsTextToSpeechModel;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /api/v1/donations creates donation with PENDING_PAYMENT status and persists trimmed values")
    void createDonationPersistsRecord() throws Exception {
        final String body = """
                {
                  "streamerId": "  %s  ",
                  "senderName": "  Alice  ",
                  "amount": 12.50,
                  "currency": "usd",
                  "messageText": "  great stream  ",
                  "voiceProfile": "voice-1"
                }
                """.formatted(STREAMER_ID);

        final MvcResult result = mockMvc.perform(post(DONATIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(DonationStatus.PENDING_PAYMENT.name()))
                .andExpect(jsonPath("$.ttsStatus").value(TtsStatus.PENDING.name()))
                .andExpect(jsonPath("$.streamerId").value(STREAMER_ID))
                .andExpect(jsonPath("$.senderName").value("Alice"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.messageText").value("great stream"))
                .andReturn();

        final long donationId = donationIdFrom(result);
        final Donation persisted = donationRepository.findById(donationId).orElseThrow();
        assertThat(persisted.getAmount()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(persisted.getStatus()).isEqualTo(DonationStatus.PENDING_PAYMENT);
        assertThat(persisted.getTtsStatus()).isEqualTo(TtsStatus.PENDING);
        verify(aiReaderService, never()).queueForReading(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/donations rejects invalid payloads with 400 and does not persist")
    void createDonationRejectsInvalidPayload() throws Exception {
        final long initialCount = donationRepository.count();
        final String invalidBody = """
                {
                  "streamerId": "",
                  "senderName": "",
                  "amount": -1,
                  "currency": "",
                  "messageText": ""
                }
                """;

        mockMvc.perform(post(DONATIONS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));

        assertThat(donationRepository.count()).isEqualTo(initialCount);
        verify(aiReaderService, never()).queueForReading(any(), any());
    }

    @Test
    @DisplayName("POST /{id}/mark-paid flips status to PAID, enqueues overlay event, and submits to TTS")
    void markPaidEnqueuesOverlayAndQueuesTts() throws Exception {
        doAnswer(invocation -> {
            final Consumer<TtsStatus> updater = invocation.getArgument(1);
            updater.accept(TtsStatus.PROCESSING);
            updater.accept(TtsStatus.COMPLETED);
            return null;
        }).when(aiReaderService).queueForReading(any(), any());

        final Donation created = newPendingDonation("Bob", "thanks!");

        mockMvc.perform(post(DONATIONS_ENDPOINT + "/" + created.getId() + "/mark-paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.status").value(DonationStatus.PAID.name()));

        final Donation reloaded = donationRepository.findById(created.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(DonationStatus.PAID);
        assertThat(reloaded.getTtsStatus()).isEqualTo(TtsStatus.COMPLETED);

        final var overlayEvents = overlayEventRepository.findNewEvents(STREAMER_ID, 0L, 50L);
        assertThat(overlayEvents)
                .extracting(OverlayEvent::getDonationId)
                .containsExactly(created.getId());
        verify(aiReaderService, times(1)).queueForReading(any(), any());
    }

    @Test
    @DisplayName("POST /{id}/mark-paid is idempotent and only enqueues overlay/TTS once")
    void markPaidIsIdempotent() throws Exception {
        final Donation created = newPendingDonation("Carol", "first");
        donationService.markPaid(created.getId());

        mockMvc.perform(post(DONATIONS_ENDPOINT + "/" + created.getId() + "/mark-paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(DonationStatus.PAID.name()));

        verify(aiReaderService, times(1)).queueForReading(any(), any());
        final long overlayEvents = overlayEventRepository.findNewEvents(STREAMER_ID, 0L, 50L)
                .stream()
                .filter(e -> e.getDonationId().equals(created.getId()))
                .count();
        assertThat(overlayEvents).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /{id}/mark-paid returns 404 for unknown donations")
    void markPaidReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(post(DONATIONS_ENDPOINT + "/999999/mark-paid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("donation_not_found"));
        verify(aiReaderService, never()).queueForReading(any(), any());
    }

    @Test
    @DisplayName("TTS lifecycle transitions follow QUEUED → PROCESSING → COMPLETED")
    void ttsLifecycleHappyPath() {
        final Donation created = newPendingDonation("Dave", "rolling");
        donationService.markPaid(created.getId());
        forceTtsStatus(created.getId(), TtsStatus.QUEUED);

        final Donation processing = donationService.markTtsProcessing(created.getId());
        assertThat(processing.getTtsStatus()).isEqualTo(TtsStatus.PROCESSING);

        final Donation completed = donationService.markTtsCompleted(created.getId());
        assertThat(completed.getTtsStatus()).isEqualTo(TtsStatus.COMPLETED);
    }

    @Test
    @DisplayName("TTS retry only allowed from FAILED state and re-submits to AiReaderService")
    void retryFailedTtsRequeuesJob() {
        final Donation created = newPendingDonation("Erin", "retry me");
        forceTtsStatus(created.getId(), TtsStatus.FAILED);

        final Donation requeued = donationService.retryFailedTts(created.getId());
        assertThat(requeued.getTtsStatus()).isEqualTo(TtsStatus.QUEUED);
        verify(aiReaderService).queueForReading(any(), any());
    }

    @Test
    @DisplayName("TTS transition methods reject invalid source states")
    void ttsTransitionsRejectInvalidStates() {
        final Donation created = newPendingDonation("Frank", "bad transition");
        forceTtsStatus(created.getId(), TtsStatus.PENDING);

        assertThat(catchThrowable(() -> donationService.markTtsProcessing(created.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(catchThrowable(() -> donationService.markTtsCompleted(created.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(catchThrowable(() -> donationService.markTtsFailed(created.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(catchThrowable(() -> donationService.retryFailedTts(created.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Polling overlay events surfaces only newly enqueued donations")
    void overlayPollSurfacesPaidDonationOnly() throws Exception {
        final Donation pending = newPendingDonation("Gina", "still pending");
        final Donation paidLater = newPendingDonation("Henry", "going to pay");
        donationService.markPaid(paidLater.getId());

        mockMvc.perform(get("/api/v1/overlay/events")
                        .param("streamerId", STREAMER_ID)
                        .param("cursor", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.events[0].donationId").value(paidLater.getId()));

        assertThat(overlayEventRepository.findNewEvents(STREAMER_ID, 0L, 50L))
                .extracting(OverlayEvent::getDonationId)
                .doesNotContain(pending.getId());
    }

    private Donation newPendingDonation(String senderName, String message) {
        return donationService.createDonation(
                new xyz._3.social.model.request.CreateDonationInput(
                        STREAMER_ID,
                        senderName,
                        new BigDecimal("5.00"),
                        "USD",
                        message,
                        null
                )
        );
    }

    private void forceTtsStatus(long donationId, TtsStatus status) {
        final Donation donation = donationRepository.findById(donationId).orElseThrow();
        donationRepository.save(new Donation(
                donation.getId(),
                donation.getStreamerId(),
                donation.getSenderName(),
                donation.getAmount(),
                donation.getCurrency(),
                donation.getMessageText(),
                donation.getVoiceProfile(),
                status,
                donation.getStatus(),
                donation.getCreatedAt()
        ));
    }

    private long donationIdFrom(MvcResult result) throws Exception {
        final JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private static Throwable catchThrowable(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }
}
