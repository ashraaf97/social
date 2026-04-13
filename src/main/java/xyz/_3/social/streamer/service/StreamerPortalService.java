package xyz._3.social.streamer.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import xyz._3.social.donation.domain.Donation;
import xyz._3.social.donation.repository.DonationRepository;
import xyz._3.social.donation.service.DonationNotFoundException;
import xyz._3.social.overlay.service.OverlayService;

@Service
public class StreamerPortalService {
    private final DonationRepository donationRepository;
    private final OverlayService overlayService;

    public StreamerPortalService(DonationRepository donationRepository, OverlayService overlayService) {
        this.donationRepository = donationRepository;
        this.overlayService = overlayService;
    }

    public Flux<Donation> listDonations(String streamerId, int page, int size) {
        long limit = Math.max(1, size);
        long offset = Math.max(0, page) * limit;
        return donationRepository.findByStreamerIdOrderByCreatedAtDesc(streamerId, limit, offset);
    }

    public Mono<Void> replayDonation(String streamerId, long donationId) {
        return donationRepository.findById(donationId)
                .switchIfEmpty(Mono.error(new DonationNotFoundException(donationId)))
                .flatMap(donation -> {
                    if (!donation.streamerId().equals(streamerId)) {
                        return Mono.error(new DonationNotFoundException(donationId));
                    }
                    return overlayService.enqueueDonation(donation).then();
                });
    }
}
