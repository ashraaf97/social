package xyz._3.social.service;

import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import xyz._3.social.model.Donation;
import xyz._3.social.model.TtsStatus;

@Service
public class NoOpAiReaderService implements AiReaderService {

    @Override
    public void queueForReading(Donation donation, Consumer<TtsStatus> statusUpdater) {
        statusUpdater.accept(TtsStatus.SKIPPED);
    }
}
