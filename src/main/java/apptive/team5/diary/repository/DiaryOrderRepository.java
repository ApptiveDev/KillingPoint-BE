package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DiaryOrderRepository extends JpaRepository<DiaryOrderEntity, Long> {
    Optional<DiaryOrderEntity> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    @Query("select do from DiaryOrderEntity do where do.user.id in :userIds")
    List<DiaryOrderEntity> findByUserIds(Set<Long> userIds);
}
