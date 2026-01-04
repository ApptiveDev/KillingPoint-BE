package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.dto.DiaryReportRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class DiaryReportService {

    private final DiaryReportLowService diaryReportLowService;
    private final DiaryLowService diaryLowService;

    public DiaryReportEntity createDiaryReport(DiaryReportRequestDto diaryReportRequestDto, Long diaryId) {

        DiaryEntity reportedDiary = diaryLowService.findDiaryById(diaryId);

        return diaryReportLowService.save(new DiaryReportEntity(diaryReportRequestDto.content(), reportedDiary.getContent(), reportedDiary));
    }
}
