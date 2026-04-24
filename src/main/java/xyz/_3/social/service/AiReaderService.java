package xyz._3.social.service;

import java.util.function.Consumer;
import xyz._3.social.model.Donation;
import xyz._3.social.model.TtsStatus;

public interface AiReaderService {

    /**
     * Submit a donation for TTS synthesis.
     * Implementations must call {@code statusUpdater} with the resulting
     * {@link TtsStatus} (e.g. PROCESSING, COMPLETED, FAILED, SKIPPED)
     * so the caller can persist the transition.
     */
    void queueForReading(Donation donation, Consumer<TtsStatus> statusUpdater);
}
