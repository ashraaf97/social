package xyz._3.social.controller;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz._3.social.exception.DonationNotFoundException;
import xyz._3.social.exception.UnauthorizedStreamerAccessException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DonationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDonationNotFound(DonationNotFoundException ex) {
        return new ErrorResponse("donation_not_found", ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(UnauthorizedStreamerAccessException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(UnauthorizedStreamerAccessException ex) {
        return new ErrorResponse("unauthorized", "Streamer credentials are invalid", Instant.now());
    }

    public record ErrorResponse(String code, String message, Instant timestamp) {
    }
}
