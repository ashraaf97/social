package xyz._3.social.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import xyz._3.social.overlay.domain.OverlayEvent;

@Repository("socialOverlayEventRepository")
public class OverlayEventRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<OverlayEvent> OVERLAY_EVENT_ROW_MAPPER = (row, rowNum) -> new OverlayEvent(
            row.getLong("id"),
            row.getLong("donation_id"),
            row.getString("streamer_id"),
            row.getString("sender_name"),
            row.getBigDecimal("amount"),
            row.getString("currency"),
            row.getString("message_text"),
            toInstant(row.getTimestamp("created_at").toLocalDateTime())
    );

    public OverlayEventRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OverlayEvent save(OverlayEvent event) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("donationId", event.donationId())
                .addValue("streamerId", event.streamerId())
                .addValue("senderName", event.senderName())
                .addValue("amount", event.amount())
                .addValue("currency", event.currency())
                .addValue("messageText", event.messageText())
                .addValue("createdAt", LocalDateTime.ofInstant(event.createdAt(), ZoneOffset.UTC));
        jdbcTemplate.update(
                """
                INSERT INTO overlay_events(donation_id, streamer_id, sender_name, amount, currency, message_text, created_at)
                VALUES (:donationId, :streamerId, :senderName, :amount, :currency, :messageText, :createdAt)
                """,
                params,
                keyHolder,
                new String[]{"id"}
        );
        Number id = keyHolder.getKey();
        return new OverlayEvent(
                id.longValue(),
                event.donationId(),
                event.streamerId(),
                event.senderName(),
                event.amount(),
                event.currency(),
                event.messageText(),
                event.createdAt()
        );
    }

    public List<OverlayEvent> findNewEvents(String streamerId, long cursor, long limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("streamerId", streamerId)
                .addValue("cursor", cursor)
                .addValue("limit", limit);
        return jdbcTemplate.query(
                """
                SELECT * FROM overlay_events
                WHERE streamer_id = :streamerId AND id > :cursor
                ORDER BY id ASC LIMIT :limit
                """,
                params,
                OVERLAY_EVENT_ROW_MAPPER
        );
    }

    private static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
