package com.beam.social.repository;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.beam.social.model.ActiveToken;

@Repository
public interface ActiveTokenRepository extends JpaRepository<ActiveToken, String> {
    boolean existsByJtiAndExpiresAtAfter(String jti, Instant now);
    void deleteByUsername(String username);
    void deleteByExpiresAtBefore(Instant now);
}
