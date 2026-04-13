package xyz._3.social.model.request;

import java.math.BigDecimal;

public final class CreateDonationInput {
    private final String streamerId;
    private final String senderName;
    private final BigDecimal amount;
    private final String currency;
    private final String messageText;
    private final String voiceProfile;

    public CreateDonationInput(
            String streamerId,
            String senderName,
            BigDecimal amount,
            String currency,
            String messageText,
            String voiceProfile
    ) {
        this.streamerId = streamerId;
        this.senderName = senderName;
        this.amount = amount;
        this.currency = currency;
        this.messageText = messageText;
        this.voiceProfile = voiceProfile;
    }

    public String getStreamerId() {
        return streamerId;
    }

    public String getSenderName() {
        return senderName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getVoiceProfile() {
        return voiceProfile;
    }
}
