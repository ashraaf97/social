package xyz._3.social.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("TTS_AUDIO")
public record TtsAudio(
        @Id Long donationId,
        byte[] audioData,
        Instant createdAt
) {
}
