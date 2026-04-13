package xyz._3.social.donation.web;

import java.math.BigDecimal;
import java.time.Instant;
import xyz._3.social.donation.domain.Donation;
import xyz._3.social.donation.domain.DonationStatus;
import xyz._3.social.donation.domain.TtsStatus;

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
    public static DonationResponse from(Donation donation) {
        return new DonationResponse(
                donation.id(),
                donation.streamerId(),
                donation.senderName(),
                donation.amount(),
                donation.currency(),
                donation.messageText(),
                donation.voiceProfile(),
                donation.ttsStatus(),
                donation.status(),
                donation.createdAt()
        );
    }
}
