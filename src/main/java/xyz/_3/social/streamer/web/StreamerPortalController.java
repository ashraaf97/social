package xyz._3.social.streamer.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import xyz._3.social.config.StreamerProperties;
import xyz._3.social.streamer.service.StreamerAuthService;
import xyz._3.social.streamer.service.StreamerPortalService;

@RestController
@RequestMapping("/api/v1/streamer")
public class StreamerPortalController {
    private final StreamerPortalService streamerPortalService;
    private final StreamerAuthService streamerAuthService;
    private final StreamerProperties streamerProperties;

    public StreamerPortalController(
            StreamerPortalService streamerPortalService,
            StreamerAuthService streamerAuthService,
            StreamerProperties streamerProperties
    ) {
        this.streamerPortalService = streamerPortalService;
        this.streamerAuthService = streamerAuthService;
        this.streamerProperties = streamerProperties;
    }

    @GetMapping("/donations")
    public Flux<StreamerDonationResponse> listDonations(
            @RequestHeader("X-Streamer-Key") String portalKey,
            @RequestParam(required = false) String streamerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        streamerAuthService.assertAuthorized(portalKey);
        String resolvedStreamerId = streamerId == null || streamerId.isBlank()
                ? streamerProperties.defaultId()
                : streamerId;
        return streamerPortalService.listDonations(resolvedStreamerId, page, size)
                .map(StreamerDonationResponse::from);
    }

    @PostMapping("/donations/{id}/replay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> replay(
            @RequestHeader("X-Streamer-Key") String portalKey,
            @PathVariable("id") long donationId,
            @RequestParam(required = false) String streamerId
    ) {
        streamerAuthService.assertAuthorized(portalKey);
        String resolvedStreamerId = streamerId == null || streamerId.isBlank()
                ? streamerProperties.defaultId()
                : streamerId;
        return streamerPortalService.replayDonation(resolvedStreamerId, donationId);
    }
}
