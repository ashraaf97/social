package xyz._3.social.donation.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import xyz._3.social.donation.service.DonationService;

@RestController
@RequestMapping("/api/v1/donations")
public class DonationController {
    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DonationResponse> create(@Valid @RequestBody CreateDonationRequest request) {
        return donationService.createDonation(request).map(DonationResponse::from);
    }

    @PostMapping("/{id}/mark-paid")
    public Mono<DonationResponse> markPaid(@PathVariable("id") long id) {
        return donationService.markPaid(id).map(DonationResponse::from);
    }
}
