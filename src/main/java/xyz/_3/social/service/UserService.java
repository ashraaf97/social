package xyz._3.social.service;

import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz._3.social.config.AdminProperties;
import xyz._3.social.model.User;
import xyz._3.social.model.UserRole;
import xyz._3.social.model.request.SignUpRequest;
import xyz._3.social.model.response.StreamerProfileResponse;
import xyz._3.social.repository.UserRepository;

@Service
public class UserService implements UserDetailsService, ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.username())
                .password(user.passwordHash())
                .roles(user.role().name())
                .build();
    }

    public User signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }
        User user = new User(
                null,
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                UserRole.STREAMER,
                request.username(),
                Instant.now()
        );
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public List<StreamerProfileResponse> findAllStreamers() {
        return userRepository.findByRole(UserRole.STREAMER).stream()
                .map(u -> new StreamerProfileResponse(u.id(), u.username(), u.email(), u.streamerId(), u.createdAt()))
                .toList();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByUsername(adminProperties.username())) {
            User admin = new User(
                    null,
                    adminProperties.username(),
                    passwordEncoder.encode(adminProperties.password()),
                    null,
                    UserRole.ADMIN,
                    null,
                    Instant.now()
            );
            userRepository.save(admin);
        }
    }
}
