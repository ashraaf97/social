package xyz._3.social.controller;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz._3.social.exception.DonationNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DonationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDonationNotFound(DonationNotFoundException ex) {
        return new ErrorResponse("donation_not_found", ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(IllegalArgumentException ex) {
        return new ErrorResponse("bad_request", ex.getMessage(), Instant.now());
    }

    public record ErrorResponse(String code, String message, Instant timestamp) {
    }
}
