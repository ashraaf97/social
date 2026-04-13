package xyz._3.social.model.response;

import xyz._3.social.model.DonationStatus;
import xyz._3.social.model.TtsStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record StreamerDonationResponse(
        Long id,
        String senderName,
        BigDecimal amount,
        String currency,
        String messageText,
        TtsStatus ttsStatus,
        DonationStatus status,
        Instant createdAt
) {
}
