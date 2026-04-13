package xyz._3.social.mapper;

import org.mapstruct.Mapper;
import xyz._3.social.model.response.OverlayEventResponse;
import xyz._3.social.overlay.domain.OverlayEvent;

@Mapper(componentModel = "spring")
public interface OverlayWebMapper {
    OverlayEventResponse toOverlayEventResponse(OverlayEvent event);
}
