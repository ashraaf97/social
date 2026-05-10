package xyz._3.social.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import xyz._3.social.config.ElevenLabsProperties;
import xyz._3.social.model.Donation;
import xyz._3.social.model.TtsAudio;
import xyz._3.social.model.TtsStatus;
import xyz._3.social.repository.TtsAudioRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("'${app.elevenlabs.api-key:}'.length() > 0")
public class ElevenLabsAiReaderService implements AiReaderService {

    private final ElevenLabsProperties properties;
    private final TtsAudioRepository ttsAudioRepository;

    private RestClient restClient;

    @Override
    public void queueForReading(Donation donation, Consumer<TtsStatus> statusUpdater) {
        statusUpdater.accept(TtsStatus.PROCESSING);
        try {
            final byte[] audio = synthesize(donation);
            ttsAudioRepository.save(new TtsAudio(donation.getId(), audio, Instant.now()));
            statusUpdater.accept(TtsStatus.COMPLETED);
            log.info("ElevenLabs TTS completed for donation {} ({} bytes)", donation.getId(), audio.length);
        } catch (Exception e) {
            log.error("ElevenLabs TTS failed for donation {}: {}", donation.getId(), e.getMessage(), e);
            statusUpdater.accept(TtsStatus.FAILED);
        }
    }

    private byte[] synthesize(Donation donation) {
        final String voiceId = resolveVoiceId(donation);
        final String text = buildSpeechText(donation);

        final Map<String, Object> body = Map.of(
                "text", text,
                "model_id", properties.modelId(),
                "voice_settings", Map.of(
                        "stability", 0.5,
                        "similarity_boost", 0.75
                )
        );

        return restClient()
                .post()
                .uri("/v1/text-to-speech/{voiceId}?output_format=mp3_44100_128", voiceId)
                .header("xi-api-key", properties.apiKey())
                .header("Accept", "audio/mpeg")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(byte[].class);
    }

    private RestClient restClient() {
        if (restClient == null) {
            restClient = RestClient.builder()
                    .baseUrl(properties.baseUrl())
                    .build();
        }
        return restClient;
    }

    private String resolveVoiceId(Donation donation) {
        final String donationVoice = donation.getVoiceProfile();
        if (donationVoice != null && !donationVoice.isBlank()) {
            return donationVoice.trim();
        }
        return properties.voiceId();
    }

    private static String buildSpeechText(Donation donation) {
        return "%s from %s, '%s'".formatted(
                formatAmount(donation.getAmount()),
                donation.getSenderName(),
                donation.getMessageText()
        );
    }

    private static String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}
