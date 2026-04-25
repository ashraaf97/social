package xyz._3.social.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.User;
import xyz._3.social.model.UserRole;

@Repository
public interface UserRepository extends ListPagingAndSortingRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findByRole(UserRole role, Pageable pageable);
    boolean existsByUsername(String username);
}
