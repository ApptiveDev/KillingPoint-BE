package apptive.team5.recommendation.repository;

import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreferenceEntity, Long> {

    Optional<UserGenrePreferenceEntity> findByUser_IdAndGenreNameRaw(Long userId, String genreNameRaw);

    List<UserGenrePreferenceEntity> findTop3ByUser_IdOrderByScoreDescUpdateDateTimeDesc(Long userId);
}
