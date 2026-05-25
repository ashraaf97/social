package com.beam.social.mapper;

import org.mapstruct.Mapper;
import com.beam.social.model.Donation;
import com.beam.social.model.response.DonationResponse;

@Mapper(componentModel = "spring")
public interface DonationWebMapper {
    DonationResponse toDonationResponse(Donation donation);
}
