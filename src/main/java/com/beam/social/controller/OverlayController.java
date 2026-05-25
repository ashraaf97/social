package com.beam.social.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.beam.social.mapper.OverlayWebMapper;
import com.beam.social.model.response.OverlayEventResponse;
import com.beam.social.model.response.OverlayPollResponse;
import com.beam.social.model.response.OverlaySettingsResponse;
import com.beam.social.repository.TtsAudioRepository;
import com.beam.social.service.OverlayService;
import com.beam.social.service.UserService;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/overlay")
public class OverlayController {
    private final OverlayService overlayService;
    private final OverlayWebMapper overlayWebMapper;
    private final TtsAudioRepository ttsAudioRepository;
    private final UserService userService;

    @GetMapping("/config")
    public OverlaySettingsResponse getOverlayConfig(@RequestParam String token) {
        return userService.getOverlaySettingsByOverlayToken(token);
    }

    @GetMapping("/events")
    public OverlayPollResponse poll(
            @RequestParam String token,
            @RequestParam(defaultValue = "0") long cursor,
            @RequestParam(defaultValue = "10") long limit
    ) {
        long safeLimit = Math.min(50, Math.max(1, limit));
        List<OverlayEventResponse> events = overlayService.pollEventsByToken(token, cursor, safeLimit)
                .stream()
                .map(overlayWebMapper::toOverlayEventResponse)
                .toList();
        return toResponse(cursor, events);
    }

    @GetMapping(value = "/tts/{donationId}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getTtsAudio(@PathVariable long donationId) {
        return ttsAudioRepository.findById(donationId)
                .map(audio -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .body(audio.getAudioData()))
                .orElse(ResponseEntity.notFound().build());
    }

    private OverlayPollResponse toResponse(long currentCursor, List<OverlayEventResponse> events) {
        long nextCursor = events.isEmpty() ? currentCursor : events.get(events.size() - 1).id();
        return new OverlayPollResponse(nextCursor, events);
    }
}
