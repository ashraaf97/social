package xyz._3.social.overlay.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import xyz._3.social.overlay.service.OverlayService;

@RestController
@RequestMapping("/api/v1/overlay")
public class OverlayController {
    private final OverlayService overlayService;

    public OverlayController(OverlayService overlayService) {
        this.overlayService = overlayService;
    }

    @GetMapping("/events")
    public Mono<OverlayPollResponse> poll(
            @RequestParam String streamerId,
            @RequestParam(defaultValue = "0") long cursor,
            @RequestParam(defaultValue = "10") long limit
    ) {
        long safeLimit = Math.min(50, Math.max(1, limit));
        return overlayService.pollEvents(streamerId, cursor, safeLimit)
                .map(OverlayEventResponse::from)
                .collectList()
                .map(events -> toResponse(cursor, events));
    }

    private OverlayPollResponse toResponse(long currentCursor, List<OverlayEventResponse> events) {
        long nextCursor = events.isEmpty() ? currentCursor : events.get(events.size() - 1).id();
        return new OverlayPollResponse(nextCursor, events);
    }
}
