package xyz._3.social.donation.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import xyz._3.social.donation.domain.DonationStatus;
import xyz._3.social.donation.domain.TtsStatus;
import xyz._3.social.donation.domain.Donation;

@Repository
public class DonationRepository {
    private final DatabaseClient databaseClient;

    public DonationRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Donation> save(Donation donation) {
        if (donation.id() == null) {
            return insert(donation);
        }
        return update(donation);
    }

    public Mono<Donation> findById(long id) {
        return databaseClient.sql("SELECT * FROM donations WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> mapDonation(row))
                .one();
    }

    public Flux<Donation> findByStreamerIdOrderByCreatedAtDesc(String streamerId, long limit, long offset) {
        return databaseClient.sql("""
                        SELECT * FROM donations
                        WHERE streamer_id = :streamerId
                        ORDER BY created_at DESC
                        LIMIT :limit OFFSET :offset
                        """)
                .bind("streamerId", streamerId)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, metadata) -> mapDonation(row))
                .all();
    }

    private Mono<Donation> insert(Donation donation) {
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO donations(streamer_id, sender_name, amount, currency, message_text, voice_profile, tts_status, status, created_at)
                        VALUES (:streamerId, :senderName, :amount, :currency, :messageText, :voiceProfile, :ttsStatus, :status, :createdAt)
                        """)
                .bind("streamerId", donation.streamerId())
                .bind("senderName", donation.senderName())
                .bind("amount", donation.amount())
                .bind("currency", donation.currency())
                .bind("messageText", donation.messageText())
                .bind("ttsStatus", donation.ttsStatus().name())
                .bind("status", donation.status().name())
                .bind("createdAt", donation.createdAt());

        if (donation.voiceProfile() == null) {
            spec = spec.bindNull("voiceProfile", String.class);
        } else {
            spec = spec.bind("voiceProfile", donation.voiceProfile());
        }

        return spec.fetch().rowsUpdated()
                .then(databaseClient.sql("""
                                SELECT * FROM donations
                                WHERE streamer_id = :streamerId
                                ORDER BY id DESC
                                LIMIT 1
                                """)
                        .bind("streamerId", donation.streamerId())
                        .map((row, metadata) -> mapDonation(row))
                        .one());
    }

    private Mono<Donation> update(Donation donation) {
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE donations
                        SET streamer_id = :streamerId,
                            sender_name = :senderName,
                            amount = :amount,
                            currency = :currency,
                            message_text = :messageText,
                            voice_profile = :voiceProfile,
                            tts_status = :ttsStatus,
                            status = :status,
                            created_at = :createdAt
                        WHERE id = :id
                        """)
                .bind("streamerId", donation.streamerId())
                .bind("senderName", donation.senderName())
                .bind("amount", donation.amount())
                .bind("currency", donation.currency())
                .bind("messageText", donation.messageText())
                .bind("ttsStatus", donation.ttsStatus().name())
                .bind("status", donation.status().name())
                .bind("createdAt", donation.createdAt())
                .bind("id", donation.id());

        if (donation.voiceProfile() == null) {
            spec = spec.bindNull("voiceProfile", String.class);
        } else {
            spec = spec.bind("voiceProfile", donation.voiceProfile());
        }

        return spec.fetch().rowsUpdated().thenReturn(donation);
    }

    private Donation mapDonation(io.r2dbc.spi.Row row) {
        return new Donation(
                row.get("id", Long.class),
                row.get("streamer_id", String.class),
                row.get("sender_name", String.class),
                row.get("amount", java.math.BigDecimal.class),
                row.get("currency", String.class),
                row.get("message_text", String.class),
                row.get("voice_profile", String.class),
                TtsStatus.valueOf(row.get("tts_status", String.class)),
                DonationStatus.valueOf(row.get("status", String.class)),
                toInstant(row.get("created_at", LocalDateTime.class))
        );
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
