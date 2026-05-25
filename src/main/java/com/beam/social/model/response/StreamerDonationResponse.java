package com.beam.social.model.response;

import com.beam.social.model.DonationStatus;
import com.beam.social.model.TtsStatus;

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
