package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryOrderRepository extends JpaRepository<DiaryOrderEntity, Long> {
    Optional<DiaryOrderEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
