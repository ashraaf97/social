package xyz._3.social.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import xyz._3.social.exception.DonationNotFoundException;
import xyz._3.social.model.Donation;
import xyz._3.social.repository.DonationRepository;

@Service
public class StreamerPortalService {
    private final DonationRepository donationRepository;
    private final OverlayService overlayService;

    public StreamerPortalService(DonationRepository donationRepository, OverlayService overlayService) {
        this.donationRepository = donationRepository;
        this.overlayService = overlayService;
    }

    public List<Donation> listDonations(String streamerId, int page, int size) {
        List<Donation> donations = donationRepository.findByStreamerIdOrderByCreatedAtDesc(streamerId);
        int safeSize = Math.max(1, size);
        int fromIndex = Math.max(0, page) * safeSize;
        if (fromIndex >= donations.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + safeSize, donations.size());
        return donations.subList(fromIndex, toIndex);
    }

    public void replayDonation(String streamerId, long donationId) {
        Optional<Donation> donation = donationRepository.findById(donationId);
        if (donation.isEmpty() || !donation.get().streamerId().equals(streamerId)) {
            throw new DonationNotFoundException(donationId);
        }
        overlayService.enqueueDonation(donation.get());
    }
}
