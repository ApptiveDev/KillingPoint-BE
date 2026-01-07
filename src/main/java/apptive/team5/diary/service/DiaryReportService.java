package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.dto.AiDiaryReportRequestDto;
import apptive.team5.diary.dto.AiDiaryReportResponseDto;
import apptive.team5.diary.dto.DiaryReportRequestDto;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
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

    public void processReportedDiary(List<Long> invalidDiaryIds) {

        diaryReportLowService.deleteAllWithBulk();

        if (invalidDiaryIds.isEmpty()) return;

        diaryLikeLowService.deleteByDiaryIds(invalidDiaryIds);

        Set<Long> userIds = diaryLowService.findAllByIds(invalidDiaryIds)
                .stream()
                .map(diary -> diary.getUser().getId())
                .collect(Collectors.toSet());

        diaryOrderLowService.deleteByDiaryIds(userIds, invalidDiaryIds);

        diaryLowService.deleteByDiaryIds(invalidDiaryIds);
    }
}


