package xyz._3.social.service;

import xyz._3.social.model.Donation;

public interface AiReaderService {
    void queueForReading(Donation donation);
}
