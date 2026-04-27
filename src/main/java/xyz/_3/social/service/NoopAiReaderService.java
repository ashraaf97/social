package xyz._3.social.service;

import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import xyz._3.social.model.Donation;
import xyz._3.social.model.TtsStatus;

/**
 * Fallback {@link AiReaderService} used when no real TTS provider (such as ElevenLabs)
 * is available. Marks queued donations as SKIPPED so the system stays consistent.
 */
@Slf4j
@Service
@ConditionalOnMissingBean(value = AiReaderService.class, ignored = NoopAiReaderService.class)
public class NoopAiReaderService implements AiReaderService {

    @Override
    public void queueForReading(Donation donation, Consumer<TtsStatus> statusUpdater) {
        log.debug("No AI reader configured, skipping TTS for donation {}", donation.getId());
        statusUpdater.accept(TtsStatus.SKIPPED);
    }
}
