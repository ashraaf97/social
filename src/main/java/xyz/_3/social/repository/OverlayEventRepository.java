package xyz._3.social.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.OverlayEvent;

@Repository("socialOverlayEventRepository")
public interface OverlayEventRepository extends JpaRepository<OverlayEvent, Long> {

    @Query(value = "SELECT * FROM overlay_events WHERE streamer_id = :streamerId AND id > :cursor ORDER BY id ASC LIMIT :limit",
            nativeQuery = true)
    List<OverlayEvent> findNewEvents(
            @Param("streamerId") String streamerId,
            @Param("cursor") long cursor,
            @Param("limit") long limit);
}
