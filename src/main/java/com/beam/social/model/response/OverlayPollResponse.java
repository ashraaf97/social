package com.beam.social.model.response;

import java.util.List;

public record OverlayPollResponse(
        long nextCursor,
        List<OverlayEventResponse> events
) {
}
