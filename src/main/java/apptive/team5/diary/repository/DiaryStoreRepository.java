package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryStoreRepository extends JpaRepository<DiaryStoreEntity, Long> {

    boolean existsByUserAndDiary(UserEntity user, DiaryEntity diary);

    DiaryStoreEntity findByUserAndDiary(UserEntity user, DiaryEntity diary);
}
