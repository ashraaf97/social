package com.beam.social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.beam.social.model.TtsAudio;

@Repository
public interface TtsAudioRepository extends JpaRepository<TtsAudio, Long> {
}
