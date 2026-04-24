package xyz._3.social.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz._3.social.model.TtsAudio;

@Repository
public interface TtsAudioRepository extends CrudRepository<TtsAudio, Long> {
}
