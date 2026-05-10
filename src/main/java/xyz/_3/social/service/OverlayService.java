package xyz._3.social.service;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz._3.social.model.Donation;
import xyz._3.social.model.OverlayEvent;
import xyz._3.social.model.User;
import xyz._3.social.repository.OverlayEventRepository;
import xyz._3.social.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class OverlayService {
    private static final Duration EVENT_EXPIRY = Duration.ofMinutes(30);
    
    private final OverlayEventRepository overlayEventRepository;
    private final UserRepository userRepository;

    public OverlayEvent enqueueDonation(Donation donation) {
        OverlayEvent event = new OverlayEvent(
                null,
                donation.getId(),
                donation.getStreamerId(),
                donation.getSenderName(),
                donation.getAmount(),
                donation.getCurrency(),
                donation.getMessageText(),
                Instant.now()
        );
        return overlayEventRepository.save(event);
    }

    public List<OverlayEvent> pollEvents(String streamerId, long cursor, long limit) {
        return overlayEventRepository.findNewEvents(streamerId, cursor, limit);
    }

    public List<OverlayEvent> pollEventsByToken(String token, long cursor, long limit) {
        final User user = userRepository.findByOverlayToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid overlay token"));
        return overlayEventRepository.findNewEvents(user.getStreamerId(), cursor, limit);
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cleanupOldEvents() {
        final Instant threshold = Instant.now().minus(EVENT_EXPIRY);
        overlayEventRepository.deleteOldEvents(threshold);
    }
}
