package xyz._3.social.model.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CreateDonationInput {
    private final String streamerId;
    private final String senderName;
    private final BigDecimal amount;
    private final String currency;
    private final String messageText;
    private final String voiceProfile;
}
