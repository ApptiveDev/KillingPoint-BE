package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.dto.DiaryReportRequestDto;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class DiaryReportService {

    private final DiaryReportLowService diaryReportLowService;
    private final DiaryLowService diaryLowService;
    private final DiaryOrderLowService diaryOrderLowService;
    private final DiaryLikeLowService diaryLikeLowService;
    private final UserLowService userLowService;

    public DiaryReportEntity createDiaryReport(DiaryReportRequestDto diaryReportRequestDto, Long diaryId, Long userId) {

        UserEntity userEntity = userLowService.getReferenceById(userId);

        DiaryEntity reportedDiary = diaryLowService.findDiaryById(diaryId);

        if (diaryReportLowService.existsByUserId(userEntity, reportedDiary))
            throw new DuplicateException(ExceptionCode.DUPLICATE_DIARY_REPORT.getDescription());

        return diaryReportLowService.save(new DiaryReportEntity(diaryReportRequestDto.content(), reportedDiary.getContent(), reportedDiary, userEntity));
    }

    public void processReportedDiary(List<Long> invalidDiaryIds, List<DiaryReportEntity> recentTop10DiaryReport) {

        List<Long> processedReportedIds = recentTop10DiaryReport.stream()
                .map(DiaryReportEntity::getId)
                .toList();

        diaryReportLowService.deleteByIds(processedReportedIds); // 처리 완료된 DiaryReport 삭제

        if (invalidDiaryIds.isEmpty()) return; // 부적절한 Diary 없으면 종료

        // 부적절한 diary 삭제 절차 시작

        diaryReportLowService.deleteByDiaryIds(invalidDiaryIds); // 부적절한 diary의 Report 모두 삭제

        diaryLikeLowService.deleteByDiaryIds(invalidDiaryIds);  // 부적절한 diary의 좋아요 삭제

        Set<Long> userIds = diaryLowService.findAllByIds(invalidDiaryIds)
                .stream()
                .map(diary -> diary.getUser().getId())
                .collect(Collectors.toSet());

        diaryOrderLowService.deleteByDiaryIds(userIds, invalidDiaryIds); // 부적절한 diary의 정렬 삭제

        diaryLowService.deleteByDiaryIds(invalidDiaryIds);
    }
}


