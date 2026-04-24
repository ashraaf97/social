package xyz._3.social.repository;

import java.time.Instant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.ActiveToken;

@Repository
public interface ActiveTokenRepository extends CrudRepository<ActiveToken, String> {
    boolean existsByJtiAndExpiresAtAfter(String jti, Instant now);
    void deleteByUsername(String username);
    void deleteByExpiresAtBefore(Instant now);
}
