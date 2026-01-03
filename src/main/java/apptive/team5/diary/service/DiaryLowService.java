package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.domain.model.DiaryInfo;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryLowService {
    private final DiaryRepository diaryRepository;

    public DiaryEntity saveDiary(DiaryEntity diary) {
        return diaryRepository.save(diary);
    }

    @Transactional(readOnly = true)
    public DiaryEntity findDiaryById(Long diaryId) {
        return diaryRepository.findById(diaryId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.NOT_FOUND_DIARY.getDescription()));
    }

    @Transactional(readOnly = true)
    public Page<DiaryEntity> findDiaryByUser(UserEntity user, Pageable pageable) {
        return diaryRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public List<DiaryEntity> findDiaryByUserId(Long userId) {
        return diaryRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<DiaryEntity> findAllByIds(List<Long> ids) {
        return diaryRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Page<DiaryEntity> findDiaryByUserAndScopeIn(Long userId, List<DiaryScope> scopes, Pageable pageable) {
        return diaryRepository.findByUserIdAndScopeIn(userId, scopes, pageable);
    }

    @Transactional(readOnly = true)
    public List<DiaryEntity> findByUserIdAndPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        return diaryRepository.findByUserIdAndCreateDateTimeBetween(userId, start, end);
    }

    public void updateDiary(DiaryEntity diary, DiaryInfo diaryInfo) {
        diary.update(diaryInfo);
    }

    public void deleteDiary(DiaryEntity diary) {
        diaryRepository.delete(diary);
    }

    @Transactional(readOnly = true)
    public int countByUserId(Long userId) {
        return diaryRepository.countByUserId(userId);
    }

    public void deleteByUserId(Long userId) {
        diaryRepository.deleteByUserId(userId);
    }


    public Page<DiaryEntity> findByUserIdsAndScopseWithUserPage(Set<Long> userIds, List<DiaryScope> scopes, Pageable pageable) {
        return diaryRepository.findByUserIdsAndScopseWithUserPage(userIds, scopes, pageable);
    }

}
