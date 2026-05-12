package xyz._3.social.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.User;
import xyz._3.social.model.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByOverlayToken(String overlayToken);
    Optional<User> findByDonationToken(String donationToken);
    Optional<User> findByStreamerId(String streamerId);
    Page<User> findByRole(UserRole role, Pageable pageable);
    boolean existsByUsername(String username);
}
