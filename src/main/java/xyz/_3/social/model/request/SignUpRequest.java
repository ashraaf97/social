package xyz._3.social.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank @Size(min = 3, max = 32) @Pattern(regexp = "[a-zA-Z0-9_-]+")
        String username,

        @NotBlank @Size(min = 8, max = 64)
        String password,

        @Email
        String email
) {
}
