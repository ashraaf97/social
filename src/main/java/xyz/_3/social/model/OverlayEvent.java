package xyz._3.social.model;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("OVERLAY_EVENTS")
public record OverlayEvent(
        @Id Long id,
        Long donationId,
        String streamerId,
        String senderName,
        BigDecimal amount,
        String currency,
        String messageText,
        Instant createdAt
) {
}
