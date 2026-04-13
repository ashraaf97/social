package xyz._3.social.model;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("DONATIONS")
public record Donation(
        @Id Long id,
        String streamerId,
        String senderName,
        BigDecimal amount,
        String currency,
        String messageText,
        String voiceProfile,
        TtsStatus ttsStatus,
        DonationStatus status,
        Instant createdAt
) {
}
