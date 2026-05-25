package com.beam.social.mapper;

import org.mapstruct.Mapper;
import com.beam.social.model.Donation;
import com.beam.social.model.response.StreamerDonationResponse;

@Mapper(componentModel = "spring")
public interface StreamerWebMapper {
    StreamerDonationResponse toStreamerDonationResponse(Donation donation);
}
