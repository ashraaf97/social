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
import xyz._3.social.service.JwtService;
import xyz._3.social.service.TokenBlacklistService;
import xyz._3.social.service.UserService;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@RequestBody @Valid SignUpRequest request) {
        User user = userService.signUp(request);
        return new AuthResponse(jwtService.generateToken(user), user.role(), user.streamerId());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userService.findByUsername(request.username());
        return new AuthResponse(jwtService.generateToken(user), user.role(), user.streamerId());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
        }
    }
}
