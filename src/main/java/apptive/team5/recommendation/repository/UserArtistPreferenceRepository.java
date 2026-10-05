package apptive.team5.recommendation.repository;

import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface UserArtistPreferenceRepository extends JpaRepository<UserArtistPreferenceEntity, Long> {

    Optional<UserArtistPreferenceEntity> findByUser_IdAndSourceArtistId(Long userId, String sourceArtistId);

    List<UserArtistPreferenceEntity> findTop5ByUser_IdOrderByScoreDescUpdateDateTimeDesc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserArtistPreferenceEntity e where e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
