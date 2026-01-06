package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.repository.DiaryReportRepository;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class DiaryReportLowService {

    private final DiaryReportRepository diaryReportRepository;

    public DiaryReportEntity save(DiaryReportEntity diaryReportEntity) {
        return diaryReportRepository.save(diaryReportEntity);
    }

    public void deleteByDiaryId(Long diaryId) {
        diaryReportRepository.deleteByDiaryId(diaryId);
    }

    public void deleteByDiaryIds(List<Long> diaryIds) {
        diaryReportRepository.deleteByDiaryIds(diaryIds);
    }

    @Transactional(readOnly = true)
    public List<DiaryReportEntity> findAll() {
        return diaryReportRepository.findAll();
    }

    public void deleteAllWithBulk() {
        diaryReportRepository.deleteAllWithBulk();
    }

    public void deleteByUserId(Long userId) {
        diaryReportRepository.deleteByUserId(userId);
    }

    public boolean existsByUserId(UserEntity user, DiaryEntity diary) {
        return diaryReportRepository.existsByUserAndDiary(user, diary);
    }
}
