package xyz._3.social.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz._3.social.exception.DonationNotFoundException;
import xyz._3.social.model.Donation;
import xyz._3.social.model.DonationStatus;
import xyz._3.social.model.response.StreamerSummaryResponse;
import xyz._3.social.repository.DonationRepository;

@RequiredArgsConstructor
@Service
public class StreamerPortalService {
    private final DonationRepository donationRepository;
    private final OverlayService overlayService;

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

    public StreamerSummaryResponse getSummary(String streamerId) {
        List<Donation> paid = donationRepository.findByStreamerIdOrderByCreatedAtDesc(streamerId)
                .stream()
                .filter(d -> d.status() == DonationStatus.PAID)
                .toList();
        BigDecimal total = paid.stream().map(Donation::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new StreamerSummaryResponse(streamerId, paid.size(), total);
    }

    public void replayDonation(String streamerId, long donationId) {
        Optional<Donation> donation = donationRepository.findById(donationId);
        if (donation.isEmpty() || !donation.get().streamerId().equals(streamerId)) {
            throw new DonationNotFoundException(donationId);
        }
        overlayService.enqueueDonation(donation.get());
    }
}
