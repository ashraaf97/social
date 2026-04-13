package xyz._3.social.ai;

import reactor.core.publisher.Mono;
import xyz._3.social.donation.domain.Donation;

public interface AiReaderService {
    Mono<Void> queueForReading(Donation donation);
}
