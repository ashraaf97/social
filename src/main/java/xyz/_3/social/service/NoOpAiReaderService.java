package xyz._3.social.service;

import org.springframework.stereotype.Service;
import xyz._3.social.model.Donation;

@Service
public class NoOpAiReaderService implements AiReaderService {
    @Override
    public void queueForReading(Donation donation) {
    }
}
