package xyz._3.social.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import xyz._3.social.model.User;
import xyz._3.social.model.request.LoginRequest;
import xyz._3.social.model.request.SignUpRequest;
import xyz._3.social.model.response.AuthResponse;
import xyz._3.social.service.ActiveTokenService;
import xyz._3.social.service.JwtService;
import xyz._3.social.service.UserService;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ActiveTokenService activeTokenService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@RequestBody @Valid SignUpRequest request) {
        final User user = userService.signUp(request);
        return issueToken(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        final User user = userService.findByUsername(request.username());
        return issueToken(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            activeTokenService.revoke(jwtService.extractJti(authHeader.substring(7)));
        }
    }

    private AuthResponse issueToken(User user) {
        final String token = jwtService.generateToken(user);
        activeTokenService.register(
                jwtService.extractJti(token),
                user.username(),
                jwtService.extractExpiration(token)
        );
        return new AuthResponse(token, user.role(), user.streamerId());
    }
}
