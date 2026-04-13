package xyz._3.social.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateDonationRequest(
        String streamerId,
        @NotBlank @Size(max = 120) String senderName,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Size(max = 16) String currency,
        @NotBlank @Size(max = 1000) String messageText,
        @Size(max = 64) String voiceProfile
) {
}
