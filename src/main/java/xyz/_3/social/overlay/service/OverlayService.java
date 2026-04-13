package xyz._3.social.overlay.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import xyz._3.social.donation.domain.Donation;
import xyz._3.social.overlay.domain.OverlayEvent;
import xyz._3.social.overlay.repository.OverlayEventRepository;

@Service
public class OverlayService {
    private final OverlayEventRepository overlayEventRepository;

    public OverlayService(OverlayEventRepository overlayEventRepository) {
        this.overlayEventRepository = overlayEventRepository;
    }

    public Mono<OverlayEvent> enqueueDonation(Donation donation) {
        OverlayEvent event = new OverlayEvent(
                null,
                donation.id(),
                donation.streamerId(),
                donation.senderName(),
                donation.amount(),
                donation.currency(),
                donation.messageText(),
                Instant.now()
        );
        return overlayEventRepository.save(event);
    }

    public Flux<OverlayEvent> pollEvents(String streamerId, long cursor, long limit) {
        return overlayEventRepository.findNewEvents(streamerId, cursor, limit);
    }
}
