package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.repository.DiaryReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class DiaryReportLowService {

    private final DiaryReportRepository diaryReportRepository;

    public DiaryReportEntity save(DiaryReportEntity diaryReportEntity) {
        return diaryReportRepository.save(diaryReportEntity);
    }
}
