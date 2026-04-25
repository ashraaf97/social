package xyz._3.social.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz._3.social.model.response.PageResponse;
import xyz._3.social.model.response.StreamerProfileResponse;
import xyz._3.social.service.UserService;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserService userService;

    @GetMapping("/streamers")
    public PageResponse<StreamerProfileResponse> listStreamers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.findAllStreamers(page, size);
    }
}
