package apptive.team5.recommendation.repository;

import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.MusicMetadataSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicMetadataRepository extends JpaRepository<MusicMetadataEntity, Long> {

    Optional<MusicMetadataEntity> findBySourceTypeAndSourceTrackId(
            MusicMetadataSourceType sourceType,
            String sourceTrackId
    );
}
