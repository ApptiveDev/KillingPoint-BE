package apptive.team5.recommendation.repository;

import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserArtistPreferenceRepository extends JpaRepository<UserArtistPreferenceEntity, Long> {

    Optional<UserArtistPreferenceEntity> findByUser_IdAndSourceArtistId(Long userId, String sourceArtistId);

    List<UserArtistPreferenceEntity> findTop5ByUser_IdOrderByScoreDescUpdateDateTimeDesc(Long userId);
}
