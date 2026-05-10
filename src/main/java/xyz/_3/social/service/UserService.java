package xyz._3.social.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz._3.social.config.AdminProperties;
import xyz._3.social.model.User;
import xyz._3.social.model.UserRole;
import xyz._3.social.model.request.SignUpRequest;
import xyz._3.social.model.response.PageResponse;
import xyz._3.social.model.response.StreamerProfileResponse;
import xyz._3.social.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService, ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
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
                generateSecureToken(),
                generateSecureToken(),
                Instant.now()
        );
        return userRepository.save(user);
    }

    public User findByDonationToken(String donationToken) {
        return userRepository.findByDonationToken(donationToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid donation token"));
    }

    private String generateSecureToken() {
        final byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public PageResponse<StreamerProfileResponse> findAllStreamers(int page, int size) {
        final var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.of(
                userRepository.findByRole(UserRole.STREAMER, pageable)
                        .map(u -> new StreamerProfileResponse(u.getId(), u.getUsername(), u.getEmail(), u.getStreamerId(), u.getCreatedAt()))
        );
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedSampleStreamer();
    }

    private void seedAdmin() {
        if (userRepository.existsByUsername(adminProperties.username())) {
            return;
        }
        final User admin = new User(
                null,
                adminProperties.username(),
                passwordEncoder.encode(adminProperties.password()),
                null,
                UserRole.ADMIN,
                null,
                null,
                null,
                Instant.now()
        );
        userRepository.save(admin);
    }

    private void seedSampleStreamer() {
        final String username = "streamer1";
        if (userRepository.existsByUsername(username)) {
            return;
        }
        final User streamer = new User(
                null,
                username,
                passwordEncoder.encode("streamer123"),
                "streamer1@example.com",
                UserRole.STREAMER,
                username,
                generateSecureToken(),
                generateSecureToken(),
                Instant.now()
        );
        userRepository.save(streamer);
    }
}
