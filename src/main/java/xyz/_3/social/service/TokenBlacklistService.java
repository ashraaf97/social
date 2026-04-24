package xyz._3.social.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiry) {
        blacklist.put(token, expiry);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    @Scheduled(fixedRateString = "PT1H")
    public void evictExpired() {
        final Instant now = Instant.now();
        blacklist.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }
}
