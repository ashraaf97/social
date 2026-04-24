package xyz._3.social.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz._3.social.model.response.OverlayEventResponse;
import xyz._3.social.model.response.OverlayPollResponse;
import xyz._3.social.mapper.OverlayWebMapper;
import xyz._3.social.service.OverlayService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/overlay")
public class OverlayController {
    private final OverlayService overlayService;
    private final OverlayWebMapper overlayWebMapper;

    @GetMapping("/events")
    public OverlayPollResponse poll(
            @RequestParam String streamerId,
            @RequestParam(defaultValue = "0") long cursor,
            @RequestParam(defaultValue = "10") long limit
    ) {
        long safeLimit = Math.min(50, Math.max(1, limit));
        List<OverlayEventResponse> events = overlayService.pollEvents(streamerId, cursor, safeLimit)
                .stream()
                .map(overlayWebMapper::toOverlayEventResponse)
                .toList();
        return toResponse(cursor, events);
    }

    private OverlayPollResponse toResponse(long currentCursor, List<OverlayEventResponse> events) {
        long nextCursor = events.isEmpty() ? currentCursor : events.get(events.size() - 1).id();
        return new OverlayPollResponse(nextCursor, events);
    }
}
