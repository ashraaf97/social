package xyz._3.social.overlay.web;

import java.math.BigDecimal;
import java.time.Instant;
import xyz._3.social.overlay.domain.OverlayEvent;

public record OverlayEventResponse(
        Long id,
        Long donationId,
        String senderName,
        BigDecimal amount,
        String currency,
        String messageText,
        Instant createdAt
) {
    public static OverlayEventResponse from(OverlayEvent event) {
        return new OverlayEventResponse(
                event.id(),
                event.donationId(),
                event.senderName(),
                event.amount(),
                event.currency(),
                event.messageText(),
                event.createdAt()
        );
    }
}
