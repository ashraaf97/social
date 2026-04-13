package xyz._3.social.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import xyz._3.social.config.StreamerProperties;
import xyz._3.social.service.StreamerAuthService;
import xyz._3.social.service.StreamerPortalService;
import xyz._3.social.model.response.StreamerDonationResponse;
import xyz._3.social.mapper.StreamerWebMapper;

@RestController
@RequestMapping("/api/v1/streamer")
public class StreamerPortalController {
    private final StreamerPortalService streamerPortalService;
    private final StreamerAuthService streamerAuthService;
    private final StreamerProperties streamerProperties;
    private final StreamerWebMapper streamerWebMapper;

    public StreamerPortalController(
            StreamerPortalService streamerPortalService,
            StreamerAuthService streamerAuthService,
            StreamerProperties streamerProperties,
            StreamerWebMapper streamerWebMapper
    ) {
        this.streamerPortalService = streamerPortalService;
        this.streamerAuthService = streamerAuthService;
        this.streamerProperties = streamerProperties;
        this.streamerWebMapper = streamerWebMapper;
    }

    @GetMapping("/donations")
    public List<StreamerDonationResponse> listDonations(
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
                .stream()
                .map(streamerWebMapper::toStreamerDonationResponse)
                .toList();
    }

    @PostMapping("/donations/{id}/replay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replay(
            @RequestHeader("X-Streamer-Key") String portalKey,
            @PathVariable("id") long donationId,
            @RequestParam(required = false) String streamerId
    ) {
        streamerAuthService.assertAuthorized(portalKey);
        String resolvedStreamerId = streamerId == null || streamerId.isBlank()
                ? streamerProperties.defaultId()
                : streamerId;
        streamerPortalService.replayDonation(resolvedStreamerId, donationId);
    }
}
