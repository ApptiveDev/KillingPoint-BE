package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.repository.DiaryStoreRepository;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class DiaryStoreLowService {

    private final DiaryStoreRepository diaryStoreRepository;

    public DiaryStoreEntity save(DiaryStoreEntity diaryStoreEntity) {
        return diaryStoreRepository.save(diaryStoreEntity);
    }

    public void deleteById(Long id) {
        diaryStoreRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserAndDiary(UserEntity user, DiaryEntity diary) {
        return diaryStoreRepository.existsByUserAndDiary(user, diary);
    }

    @Transactional(readOnly = true)
    public DiaryStoreEntity findByUserAndDiary(UserEntity user, DiaryEntity diary) {
        return diaryStoreRepository.findByUserAndDiary(user, diary);
    }

    @Transactional(readOnly = true)
    public Set<Long> findStoredDiaryIdsByUser(Long userId, List<Long> diaryIds) {
        return diaryStoreRepository.findStoredDiaryIdsByUser(userId, diaryIds);
    }

    public void deleteByDiaryId(Long diaryId) {
        diaryStoreRepository.deleteByDiaryId(diaryId);
    }

    public void deleteByDiaryIds(List<Long> diaryIds) {
        diaryStoreRepository.deleteByDiaryIds(diaryIds);
    }

    public void deleteByUserId(Long userId) {
        diaryStoreRepository.deleteByUserId(userId);
    }
}
