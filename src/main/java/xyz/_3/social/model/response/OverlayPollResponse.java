package xyz._3.social.model.response;

import java.util.List;

public record OverlayPollResponse(
        long nextCursor,
        List<OverlayEventResponse> events
) {
}
