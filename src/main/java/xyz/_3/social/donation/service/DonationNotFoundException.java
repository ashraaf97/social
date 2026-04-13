package xyz._3.social.donation.service;

public class DonationNotFoundException extends RuntimeException {
    public DonationNotFoundException(long donationId) {
        super("Donation not found: " + donationId);
    }
}
