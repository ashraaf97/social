package xyz._3.social.service;

import java.time.Instant;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.stereotype.Service;
import xyz._3.social.model.Donation;
import xyz._3.social.model.TtsAudio;
import xyz._3.social.model.TtsStatus;
import xyz._3.social.repository.TtsAudioRepository;

@Slf4j
@AllArgsConstructor
@Service
public class ElevenLabsAiReaderService implements AiReaderService {

    private final ElevenLabsTextToSpeechModel ttsModel;
    private final TtsAudioRepository ttsAudioRepository;

    @Override
    public void queueForReading(Donation donation, Consumer<TtsStatus> statusUpdater) {
        statusUpdater.accept(TtsStatus.PROCESSING);
        try {
            final TextToSpeechPrompt prompt = buildPrompt(donation);
            final byte[] audio = ttsModel.call(prompt).getResult().getOutput();
            ttsAudioRepository.save(new TtsAudio(donation.id(), audio, Instant.now()));
            statusUpdater.accept(TtsStatus.COMPLETED);
            log.info("TTS completed for donation {}", donation.id());
        } catch (Exception e) {
            log.error("TTS failed for donation {}: {}", donation.id(), e.getMessage(), e);
            statusUpdater.accept(TtsStatus.FAILED);
        }
    }

    private TextToSpeechPrompt buildPrompt(Donation donation) {
        final String text = buildSpeechText(donation);
        if (donation.voiceProfile() != null && !donation.voiceProfile().isBlank()) {
            final ElevenLabsTextToSpeechOptions options = ElevenLabsTextToSpeechOptions.builder()
                    .voiceId(donation.voiceProfile())
                    .build();
            return new TextToSpeechPrompt(text, options);
        }
        return new TextToSpeechPrompt(text);
    }

    private static String buildSpeechText(Donation donation) {
        return "%s donated %s %s and says: %s".formatted(
                donation.senderName(),
                donation.amount().toPlainString(),
                donation.currency(),
                donation.messageText()
        );
    }
}
