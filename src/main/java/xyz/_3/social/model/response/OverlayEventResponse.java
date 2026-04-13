package xyz._3.social.model.response;

import java.math.BigDecimal;
import java.time.Instant;

public record OverlayEventResponse(
        Long id,
        Long donationId,
        String senderName,
        BigDecimal amount,
        String currency,
        String messageText,
        Instant createdAt
) {
}
