package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.repository.DiaryStoreRepository;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public boolean existsByUserAndDiaryId(UserEntity user, Long diaryId) {
        return diaryStoreRepository.existsByUserAndDiaryId(user, diaryId);
    }

    @Transactional(readOnly = true)
    public DiaryStoreEntity findByUserAndDiaryId(UserEntity user, Long diaryId) {
        return diaryStoreRepository.findByUserAndDiaryId(user, diaryId);
    }

    @Transactional(readOnly = true)
    public Set<Long> findStoredDiaryIdsByUser(Long userId, List<Long> diaryIds) {
        return diaryStoreRepository.findStoredDiaryIdsByUser(userId, diaryIds);
    }

    @Transactional(readOnly = true)
    public Page<DiaryStoreEntity> findStoredDiaryByUser(Long userId, Pageable pageable) {
        return diaryStoreRepository.findStoredDiaryByUser(userId, pageable);
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
