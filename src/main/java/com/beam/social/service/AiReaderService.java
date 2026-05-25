package com.beam.social.service;

import java.util.function.Consumer;
import com.beam.social.model.Donation;
import com.beam.social.model.TtsStatus;

public interface AiReaderService {

    /**
     * Submit a donation for TTS synthesis.
     * Implementations must call {@code statusUpdater} with the resulting
     * {@link TtsStatus} (e.g. PROCESSING, COMPLETED, FAILED, SKIPPED)
     * so the caller can persist the transition.
     */
    void queueForReading(Donation donation, Consumer<TtsStatus> statusUpdater);
}
