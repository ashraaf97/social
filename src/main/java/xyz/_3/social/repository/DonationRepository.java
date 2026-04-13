package xyz._3.social.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.Donation;

@Repository("socialDonationRepository")
public interface DonationRepository extends CrudRepository<Donation, Long> {
    List<Donation> findByStreamerIdOrderByCreatedAtDesc(String streamerId);
}
