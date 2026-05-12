package xyz._3.social.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xyz._3.social.mapper.StreamerWebMapper;
import xyz._3.social.model.OverlayPosition;
import xyz._3.social.model.UserRole;
import xyz._3.social.model.request.UpdateOverlaySettingsRequest;
import xyz._3.social.model.response.OverlaySettingsResponse;
import xyz._3.social.model.response.StreamerDonationResponse;
import xyz._3.social.model.response.StreamerSummaryResponse;
import xyz._3.social.security.AuthenticatedUser;
import xyz._3.social.service.StreamerPortalService;
import xyz._3.social.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/streamer")
public class StreamerPortalController {

    private final StreamerPortalService streamerPortalService;
    private final StreamerWebMapper streamerWebMapper;
    private final UserService userService;

    @GetMapping("/donations")
    public List<StreamerDonationResponse> listDonations(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String streamerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String resolvedId = resolveStreamerId(currentUser, streamerId);
        return streamerPortalService.listDonations(resolvedId, page, size)
                .stream()
                .map(streamerWebMapper::toStreamerDonationResponse)
                .toList();
    }

    @GetMapping("/donations/summary")
    public StreamerSummaryResponse getDonationSummary(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String streamerId
    ) {
        String resolvedId = resolveStreamerId(currentUser, streamerId);
        return streamerPortalService.getSummary(resolvedId);
    }

    @PostMapping("/donations/{id}/replay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replay(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable("id") long donationId,
            @RequestParam(required = false) String streamerId
    ) {
        String resolvedId = resolveStreamerId(currentUser, streamerId);
        streamerPortalService.replayDonation(resolvedId, donationId);
    }

    @GetMapping("/overlay-settings")
    public OverlaySettingsResponse getOverlaySettings(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String streamerId
    ) {
        final String resolvedId = resolveStreamerId(currentUser, streamerId);
        return userService.getOverlaySettingsForStreamer(resolvedId);
    }

    @PutMapping("/overlay-settings")
    public OverlaySettingsResponse updateOverlaySettings(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String streamerId,
            @Valid @RequestBody UpdateOverlaySettingsRequest body
    ) {
        final String resolvedId = resolveStreamerId(currentUser, streamerId);
        final OverlayPosition position;
        try {
            position = OverlayPosition.valueOf(body.position().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid overlay position");
        }
        return userService.updateOverlaySettingsForStreamer(resolvedId, position);
    }

    @GetMapping("/overlay-url")
    public String getOverlayUrl(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest request
    ) {
        if (currentUser.role() != UserRole.STREAMER) {
            throw new IllegalArgumentException("Overlay URL is only available for streamer accounts");
        }
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
        return baseUrl + "/overlay.html?streamerId=" + currentUser.streamerId();
    }

    private String resolveStreamerId(AuthenticatedUser user, String streamerId) {
        if (user.role() == UserRole.ADMIN) {
            if (streamerId == null || streamerId.isBlank()) {
                throw new IllegalArgumentException("streamerId is required for admin access");
            }
            return streamerId;
        }
        return user.streamerId();
    }
}
