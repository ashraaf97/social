package com.beam.social.model.response;

import com.beam.social.model.DonationStatus;
import com.beam.social.model.TtsStatus;

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
