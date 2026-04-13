package xyz._3.social.ai;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import xyz._3.social.donation.domain.Donation;

@Service
public class NoOpAiReaderService implements AiReaderService {
    @Override
    public Mono<Void> queueForReading(Donation donation) {
        return Mono.empty();
    }
}
