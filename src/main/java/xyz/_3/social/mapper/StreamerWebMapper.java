package xyz._3.social.mapper;

import org.mapstruct.Mapper;
import xyz._3.social.model.Donation;
import xyz._3.social.model.response.StreamerDonationResponse;

@Mapper(componentModel = "spring")
public interface StreamerWebMapper {
    StreamerDonationResponse toStreamerDonationResponse(Donation donation);
}
