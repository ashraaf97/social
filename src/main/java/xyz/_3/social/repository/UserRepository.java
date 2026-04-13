package xyz._3.social.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.User;
import xyz._3.social.model.UserRole;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(UserRole role);
    boolean existsByUsername(String username);
}
