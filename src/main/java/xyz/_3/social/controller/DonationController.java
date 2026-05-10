package xyz._3.social.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import xyz._3.social.mapper.DonationWebMapper;
import xyz._3.social.model.User;
import xyz._3.social.model.request.CreateDonationInput;
import xyz._3.social.model.request.PublicCreateDonationRequest;
import xyz._3.social.model.response.DonationResponse;
import xyz._3.social.model.response.DonationStreamerResponse;
import xyz._3.social.service.DonationService;
import xyz._3.social.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/donations")
public class DonationController {
    private final DonationService donationService;
    private final DonationWebMapper donationWebMapper;
    private final UserService userService;

    @GetMapping("/streamer")
    public DonationStreamerResponse getStreamerByToken(@RequestParam String token) {
        final User streamer = userService.findByDonationToken(token);
        return new DonationStreamerResponse(streamer.getStreamerId(), streamer.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonationResponse create(@Valid @RequestBody PublicCreateDonationRequest request) {
        final User streamer = userService.findByDonationToken(request.donationToken());
        final CreateDonationInput input = new CreateDonationInput(
                streamer.getStreamerId(),
                request.senderName(),
                request.amount(),
                request.currency(),
                request.messageText(),
                request.voiceProfile()
        );
        return donationWebMapper.toDonationResponse(donationService.createDonation(input));
    }

    @PostMapping("/{id}/mark-paid")
    public DonationResponse markPaid(@PathVariable("id") long id) {
        return donationWebMapper.toDonationResponse(donationService.markPaid(id));
    }
}
