package xyz._3.social.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import xyz._3.social.model.request.CreateDonationRequest;
import xyz._3.social.model.response.DonationResponse;
import xyz._3.social.mapper.DonationWebMapper;
import xyz._3.social.service.DonationService;

@RestController
@RequestMapping("/api/v1/donations")
public class DonationController {
    private final DonationService donationService;
    private final DonationWebMapper donationWebMapper;

    public DonationController(DonationService donationService, DonationWebMapper donationWebMapper) {
        this.donationService = donationService;
        this.donationWebMapper = donationWebMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonationResponse create(@Valid @RequestBody CreateDonationRequest request) {
        return donationWebMapper.toDonationResponse(
                donationService.createDonation(donationWebMapper.toCreateDonationInput(request))
        );
    }

    @PostMapping("/{id}/mark-paid")
    public DonationResponse markPaid(@PathVariable("id") long id) {
        return donationWebMapper.toDonationResponse(donationService.markPaid(id));
    }
}
