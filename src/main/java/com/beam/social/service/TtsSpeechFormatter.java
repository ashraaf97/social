package com.beam.social.service;

import java.math.BigDecimal;
import com.beam.social.model.Donation;

final class TtsSpeechFormatter {

    private TtsSpeechFormatter() {
    }

    static String buildSpeechText(Donation donation) {
        return "%s from %s, '%s'".formatted(
                formatAmount(donation.getAmount()),
                donation.getSenderName(),
                donation.getMessageText()
        );
    }

    private static String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}