package xyz._3.social.donation.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Donation(
        Long id,
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
