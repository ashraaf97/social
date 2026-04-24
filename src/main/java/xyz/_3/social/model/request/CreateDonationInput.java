package xyz._3.social.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CreateDonationInput {
    @NotBlank
    private final String streamerId;
    @NotBlank @Size(max = 120)
    private final String senderName;
    @NotNull @DecimalMin(value = "0.01")
    private final BigDecimal amount;
    @NotBlank @Size(max = 16)
    private final String currency;
    @NotBlank @Size(max = 1000)
    private final String messageText;
    @Size(max = 64)
    private final String voiceProfile;
}
