package xyz._3.social.overlay.web;

import java.util.List;

public record OverlayPollResponse(
        long nextCursor,
        List<OverlayEventResponse> events
) {
}
