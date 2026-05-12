package xyz._3.social.model.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateOverlaySettingsRequest(@NotBlank String position) {}
