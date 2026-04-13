package xyz._3.social.mapper;

import org.mapstruct.Mapper;
import xyz._3.social.model.request.CreateDonationInput;
import xyz._3.social.model.request.CreateDonationRequest;
import xyz._3.social.model.Donation;
import xyz._3.social.model.response.DonationResponse;

@Mapper(componentModel = "spring")
public interface DonationWebMapper {
    CreateDonationInput toCreateDonationInput(CreateDonationRequest request);

    DonationResponse toDonationResponse(Donation donation);
}
