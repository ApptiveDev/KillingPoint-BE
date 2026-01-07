package apptive.team5.survey.repository;

import apptive.team5.survey.domain.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SurveyEntity s where s.user.id = :userId")
    void deleteByUserId(Long userId);
}
