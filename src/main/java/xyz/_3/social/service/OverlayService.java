package xyz._3.social.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import xyz._3.social.model.Donation;
import xyz._3.social.overlay.domain.OverlayEvent;
import xyz._3.social.repository.OverlayEventRepository;

@Service
public class OverlayService {
    private final OverlayEventRepository overlayEventRepository;

    public OverlayService(OverlayEventRepository overlayEventRepository) {
        this.overlayEventRepository = overlayEventRepository;
    }

    public OverlayEvent enqueueDonation(Donation donation) {
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

    public List<OverlayEvent> pollEvents(String streamerId, long cursor, long limit) {
        return overlayEventRepository.findNewEvents(streamerId, cursor, limit);
    }
}
