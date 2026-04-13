package xyz._3.social.donation.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import xyz._3.social.ai.AiReaderService;
import xyz._3.social.config.StreamerProperties;
import xyz._3.social.donation.domain.Donation;
import xyz._3.social.donation.domain.DonationStatus;
import xyz._3.social.donation.domain.TtsStatus;
import xyz._3.social.donation.repository.DonationRepository;
import xyz._3.social.donation.web.CreateDonationRequest;
import xyz._3.social.overlay.service.OverlayService;

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

    public Mono<Donation> createDonation(CreateDonationRequest request) {
        String normalizedMessage = request.messageText().trim();
        String streamerId = request.streamerId() == null || request.streamerId().isBlank()
                ? streamerProperties.defaultId()
                : request.streamerId().trim();

        Donation donation = new Donation(
                null,
                streamerId,
                request.senderName().trim(),
                request.amount(),
                request.currency().trim().toUpperCase(),
                normalizedMessage,
                request.voiceProfile(),
                TtsStatus.PENDING,
                DonationStatus.PENDING_PAYMENT,
                Instant.now()
        );

        return donationRepository.save(donation);
    }

    public Mono<Donation> markPaid(long donationId) {
        return donationRepository.findById(donationId)
                .switchIfEmpty(Mono.error(new DonationNotFoundException(donationId)))
                .flatMap(existing -> {
                    if (existing.status() == DonationStatus.PAID) {
                        return Mono.just(existing);
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
                    return donationRepository.save(paid)
                            .flatMap(saved -> overlayService.enqueueDonation(saved)
                                    .then(aiReaderService.queueForReading(saved))
                                    .thenReturn(saved));
                });
    }
}
