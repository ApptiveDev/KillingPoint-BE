package apptive.team5.recommendation.repository;

import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreferenceEntity, Long> {

    Optional<UserGenrePreferenceEntity> findByUser_IdAndGenreNameRaw(Long userId, String genreNameRaw);

    List<UserGenrePreferenceEntity> findTop3ByUser_IdOrderByScoreDescUpdateDateTimeDesc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserGenrePreferenceEntity e where e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
