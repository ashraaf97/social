package xyz._3.social.exception;

public class DonationNotFoundException extends RuntimeException {
    public DonationNotFoundException(long donationId) {
        super("Donation not found: " + donationId);
    }
}
