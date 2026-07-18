package com.beam.social.service;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.beam.social.config.OpenAiProperties;
import com.beam.social.model.Donation;
import com.beam.social.model.TtsAudio;
import com.beam.social.model.TtsStatus;
import com.beam.social.repository.TtsAudioRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression("'${app.tts.provider:elevenlabs}' == 'openai' && '${app.openai.api-key:}'.length() > 0")
public class OpenAiTtsService implements AiReaderService {

    private final OpenAiProperties properties;
    private final TtsAudioRepository ttsAudioRepository;

    private RestClient restClient;

    @Override
    public void queueForReading(Donation donation, Consumer<TtsStatus> statusUpdater) {
        statusUpdater.accept(TtsStatus.PROCESSING);
        try {
            final byte[] audio = synthesize(donation);
            ttsAudioRepository.save(new TtsAudio(donation.getId(), audio, Instant.now()));
            statusUpdater.accept(TtsStatus.COMPLETED);
            log.info("OpenAI TTS completed for donation {} ({} bytes)", donation.getId(), audio.length);
        } catch (Exception e) {
            log.error("OpenAI TTS failed for donation {}: {}", donation.getId(), e.getMessage(), e);
            statusUpdater.accept(TtsStatus.FAILED);
        }
    }

    private byte[] synthesize(Donation donation) {
        final String voice = resolveVoice(donation);
        final String text = TtsSpeechFormatter.buildSpeechText(donation);

        final Map<String, Object> body = Map.of(
                "model", properties.model(),
                "voice", voice,
                "input", text
        );

        return restClient()
                .post()
                .uri("/v1/audio/speech")
                .header("Authorization", "Bearer " + properties.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(byte[].class);
    }

    private RestClient restClient() {
        if (restClient == null) {
            restClient = RestClient.builder()
                    .baseUrl(properties.baseUrl())
                    .build();
        }
        return restClient;
    }

    private String resolveVoice(Donation donation) {
        final String donationVoice = donation.getVoiceProfile();
        if (donationVoice != null && !donationVoice.isBlank()) {
            return donationVoice.trim();
        }
        return properties.voice();
    }
}