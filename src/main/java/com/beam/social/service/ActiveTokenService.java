package com.beam.social.service;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.beam.social.model.ActiveToken;
import com.beam.social.repository.ActiveTokenRepository;

@AllArgsConstructor
@Service
public class ActiveTokenService {

    private final ActiveTokenRepository activeTokenRepository;

    public void register(String jti, String username, Instant expiresAt) {
        activeTokenRepository.save(new ActiveToken(jti, username, expiresAt));
    }

    public boolean isActive(String jti) {
        return activeTokenRepository.existsByJtiAndExpiresAtAfter(jti, Instant.now());
    }

    public void revoke(String jti) {
        activeTokenRepository.deleteById(jti);
    }

    public void revokeAll(String username) {
        activeTokenRepository.deleteByUsername(username);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void evictExpired() {
        activeTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
