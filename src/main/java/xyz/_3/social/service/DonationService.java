package xyz._3.social.service;

import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import xyz._3.social.config.StreamerProperties;
import xyz._3.social.exception.DonationNotFoundException;
import xyz._3.social.model.request.CreateDonationInput;
import xyz._3.social.model.Donation;
import xyz._3.social.model.DonationStatus;
import xyz._3.social.model.TtsStatus;
import xyz._3.social.repository.DonationRepository;

@AllArgsConstructor
@Service
public class DonationService {
    private final DonationRepository donationRepository;
    private final OverlayService overlayService;
    private final AiReaderService aiReaderService;
    private final StreamerProperties streamerProperties;

    public Donation createDonation(CreateDonationInput input) {
        final String normalizedMessage = input.getMessageText().trim();
        final String streamerId = input.getStreamerId() == null || input.getStreamerId().isBlank()
                ? streamerProperties.defaultId()
                : input.getStreamerId().trim();

        return donationRepository.save(new Donation(
                null,
                streamerId,
                input.getSenderName().trim(),
                input.getAmount(),
                input.getCurrency().trim().toUpperCase(),
                normalizedMessage,
                input.getVoiceProfile(),
                TtsStatus.PENDING,
                DonationStatus.PENDING_PAYMENT,
                Instant.now()
        ));
    }

    public Donation markPaid(long donationId) {
        final Donation existing = requireDonation(donationId);
        if (existing.status() == DonationStatus.PAID) {
            return existing;
        }

        final Donation paid = withStatus(existing, DonationStatus.PAID);
        final Donation queued = withTtsStatus(donationRepository.save(paid), TtsStatus.QUEUED);
        final Donation saved = donationRepository.save(queued);

        overlayService.enqueueDonation(saved);
        aiReaderService.queueForReading(saved,
                newTtsStatus -> donationRepository.save(withTtsStatus(saved, newTtsStatus)));
        return saved;
    }

    /** QUEUED → PROCESSING: called by a TTS worker when it picks up the job. */
    public Donation markTtsProcessing(long donationId) {
        return transitionTts(donationId, TtsStatus.QUEUED, TtsStatus.PROCESSING);
    }

    /** PROCESSING → COMPLETED: called after audio is synthesised and delivered. */
    public Donation markTtsCompleted(long donationId) {
        return transitionTts(donationId, TtsStatus.PROCESSING, TtsStatus.COMPLETED);
    }

    /** PROCESSING → FAILED: called when synthesis or delivery fails. */
    public Donation markTtsFailed(long donationId) {
        return transitionTts(donationId, TtsStatus.PROCESSING, TtsStatus.FAILED);
    }

    /** FAILED → QUEUED: re-submits a failed job for retry. */
    public Donation retryFailedTts(long donationId) {
        final Donation donation = requireDonation(donationId);
        if (donation.ttsStatus() != TtsStatus.FAILED) {
            throw new IllegalStateException(
                    "Cannot retry TTS for donation %d: current status is %s".formatted(donationId, donation.ttsStatus()));
        }
        final Donation requeued = donationRepository.save(withTtsStatus(donation, TtsStatus.QUEUED));
        aiReaderService.queueForReading(requeued,
                newTtsStatus -> donationRepository.save(withTtsStatus(requeued, newTtsStatus)));
        return requeued;
    }

    private Donation transitionTts(long donationId, TtsStatus expected, TtsStatus next) {
        final Donation donation = requireDonation(donationId);
        if (donation.ttsStatus() != expected) {
            throw new IllegalStateException(
                    "Cannot transition TTS for donation %d from %s to %s: current status is %s"
                            .formatted(donationId, expected, next, donation.ttsStatus()));
        }
        return donationRepository.save(withTtsStatus(donation, next));
    }

    private Donation requireDonation(long donationId) {
        return donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException(donationId));
    }

    private static Donation withStatus(Donation d, DonationStatus status) {
        return new Donation(d.id(), d.streamerId(), d.senderName(), d.amount(), d.currency(),
                d.messageText(), d.voiceProfile(), d.ttsStatus(), status, d.createdAt());
    }

    private static Donation withTtsStatus(Donation d, TtsStatus ttsStatus) {
        return new Donation(d.id(), d.streamerId(), d.senderName(), d.amount(), d.currency(),
                d.messageText(), d.voiceProfile(), ttsStatus, d.status(), d.createdAt());
    }
}
