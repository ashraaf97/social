package xyz._3.social.streamer.web;

import java.math.BigDecimal;
import java.time.Instant;
import xyz._3.social.donation.domain.Donation;
import xyz._3.social.donation.domain.DonationStatus;
import xyz._3.social.donation.domain.TtsStatus;

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
    public static StreamerDonationResponse from(Donation donation) {
        return new StreamerDonationResponse(
                donation.id(),
                donation.senderName(),
                donation.amount(),
                donation.currency(),
                donation.messageText(),
                donation.ttsStatus(),
                donation.status(),
                donation.createdAt()
        );
    }
}
