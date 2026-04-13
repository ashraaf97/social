package xyz._3.social.model.response;

import java.math.BigDecimal;

public record StreamerSummaryResponse(
        String streamerId,
        long totalDonations,
        BigDecimal totalAmount
) {
}
