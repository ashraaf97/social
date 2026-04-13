package xyz._3.social.model.response;

import xyz._3.social.model.DonationStatus;
import xyz._3.social.model.TtsStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record DonationResponse(
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
