package xyz._3.social.service;

import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import xyz._3.social.config.StreamerProperties;
import xyz._3.social.exception.DonationNotFoundException;
import xyz._3.social.model.request.CreateDonationInput;
import xyz._3.social.model.Donation;
import xyz._3.social.model.DonationStatus;
import xyz._3.social.model.TtsStatus;
import xyz._3.social.repository.DonationRepository;

@Service
public class DonationService {
    private final DonationRepository donationRepository;
    private final OverlayService overlayService;
    private final AiReaderService aiReaderService;
    private final StreamerProperties streamerProperties;

    public DonationService(
            DonationRepository donationRepository,
            OverlayService overlayService,
            AiReaderService aiReaderService,
            StreamerProperties streamerProperties
    ) {
        this.donationRepository = donationRepository;
        this.overlayService = overlayService;
        this.aiReaderService = aiReaderService;
        this.streamerProperties = streamerProperties;
    }

    public Donation createDonation(CreateDonationInput input) {
        String normalizedMessage = input.getMessageText().trim();
        String streamerId = input.getStreamerId() == null || input.getStreamerId().isBlank()
                ? streamerProperties.defaultId()
                : input.getStreamerId().trim();

        Donation donation = new Donation(
                null,
                streamerId,
                input.getSenderName().trim(),
                input.getAmount(),
                input.getCurrency().trim().toUpperCase(),
                normalizedMessage,
                input.getVoiceProfile(),
                TtsStatus.PENDING,
                DonationStatus.PENDING_PAYMENT,
                Instant.now()
        );

        return donationRepository.save(donation);
    }

    public Donation markPaid(long donationId) {
        Optional<Donation> existingDonation = donationRepository.findById(donationId);
        if (existingDonation.isEmpty()) {
            throw new DonationNotFoundException(donationId);
        }
        Donation existing = existingDonation.get();
        if (existing.status() == DonationStatus.PAID) {
            return existing;
        }
        Donation paid = new Donation(
                existing.id(),
                existing.streamerId(),
                existing.senderName(),
                existing.amount(),
                existing.currency(),
                existing.messageText(),
                existing.voiceProfile(),
                existing.ttsStatus(),
                DonationStatus.PAID,
                existing.createdAt()
        );
        Donation saved = donationRepository.save(paid);
        overlayService.enqueueDonation(saved);
        aiReaderService.queueForReading(saved);
        return saved;
    }
}
