package apptive.team5.recommendation.repository;

import apptive.team5.recommendation.domain.UserExploreExposureEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

public interface UserExploreExposureRepository extends JpaRepository<UserExploreExposureEntity, Long> {

    List<UserExploreExposureEntity> findByUser_IdOrderByCreateDateTimeDesc(Long userId, Pageable pageable);

    void deleteByUser_IdAndCreateDateTimeBefore(Long userId, LocalDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserExploreExposureEntity e where e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
