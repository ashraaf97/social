package xyz._3.social.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import xyz._3.social.config.JwtProperties;
import xyz._3.social.model.User;
import xyz._3.social.model.UserRole;

@Service
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_STREAMER_ID = "streamerId";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = props.expirationMs();
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.username())
                .claim(CLAIM_ROLE, user.role().name())
                .claim(CLAIM_STREAMER_ID, user.streamerId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public UserRole extractRole(String token) {
        return UserRole.valueOf(parseClaims(token).get(CLAIM_ROLE, String.class));
    }

    public String extractStreamerId(String token) {
        return parseClaims(token).get(CLAIM_STREAMER_ID, String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
