package com.beam.social.mapper;

import org.mapstruct.Mapper;
import com.beam.social.model.response.OverlayEventResponse;
import com.beam.social.model.OverlayEvent;

@Mapper(componentModel = "spring")
public interface OverlayWebMapper {
    OverlayEventResponse toOverlayEventResponse(OverlayEvent event);
}
