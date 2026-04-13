package xyz._3.social.overlay.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record OverlayEvent(
        Long id,
        Long donationId,
        String streamerId,
        String senderName,
        BigDecimal amount,
        String currency,
        String messageText,
        Instant createdAt
) {
}
