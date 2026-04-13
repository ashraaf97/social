package xyz._3.social.overlay.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import xyz._3.social.overlay.domain.OverlayEvent;

@Repository
public class OverlayEventRepository {
    private final DatabaseClient databaseClient;

    public OverlayEventRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<OverlayEvent> save(OverlayEvent event) {
        return databaseClient.sql("""
                        INSERT INTO overlay_events(donation_id, streamer_id, sender_name, amount, currency, message_text, created_at)
                        VALUES (:donationId, :streamerId, :senderName, :amount, :currency, :messageText, :createdAt)
                        """)
                .bind("donationId", event.donationId())
                .bind("streamerId", event.streamerId())
                .bind("senderName", event.senderName())
                .bind("amount", event.amount())
                .bind("currency", event.currency())
                .bind("messageText", event.messageText())
                .bind("createdAt", event.createdAt())
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("""
                                SELECT * FROM overlay_events
                                WHERE streamer_id = :streamerId
                                ORDER BY id DESC
                                LIMIT 1
                                """)
                        .bind("streamerId", event.streamerId())
                        .map((row, metadata) -> mapEvent(row))
                        .one());
    }

    public Flux<OverlayEvent> findNewEvents(String streamerId, long cursor, long limit) {
        return databaseClient.sql("""
                        SELECT * FROM overlay_events
                        WHERE streamer_id = :streamerId AND id > :cursor
                        ORDER BY id ASC LIMIT :limit
                        """)
                .bind("streamerId", streamerId)
                .bind("cursor", cursor)
                .bind("limit", limit)
                .map((row, metadata) -> mapEvent(row))
                .all();
    }

    private OverlayEvent mapEvent(io.r2dbc.spi.Row row) {
        return new OverlayEvent(
                row.get("id", Long.class),
                row.get("donation_id", Long.class),
                row.get("streamer_id", String.class),
                row.get("sender_name", String.class),
                row.get("amount", java.math.BigDecimal.class),
                row.get("currency", String.class),
                row.get("message_text", String.class),
                toInstant(row.get("created_at", LocalDateTime.class))
        );
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
