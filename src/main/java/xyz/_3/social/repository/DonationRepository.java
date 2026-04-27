package xyz._3.social.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.Donation;
import xyz._3.social.model.TtsStatus;

@Repository("socialDonationRepository")
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByStreamerIdOrderByCreatedAtDesc(String streamerId);
    List<Donation> findByTtsStatusOrderByCreatedAtAsc(TtsStatus ttsStatus);
}
