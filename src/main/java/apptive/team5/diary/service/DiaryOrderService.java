package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.dto.DiaryOrderUpdateRequestDto;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static apptive.team5.global.exception.ExceptionCode.INVALID_DIARY_LIST;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryOrderService {
    private final DiaryOrderLowService diaryOrderLowService;
    private final UserLowService userLowService;
    private final DiaryLowService diaryLowService;

    public void updateDiaryOrder(Long userId, DiaryOrderUpdateRequestDto requestDto) {
        UserEntity user = userLowService.getReferenceById(userId);

        List<Long> requestedIds = requestDto.diaryIds();

        if (requestedIds.isEmpty()) {
            diaryOrderLowService.deleteByUserId(userId);
            return;
        }

        List<DiaryEntity> diaries = diaryLowService.findAllByIds(requestedIds);

        validateDiaryIds(userId, diaries, requestedIds);

        diaryOrderLowService.saveOrder(user, requestDto.diaryIds());
    }

    private void validateDiaryIds(Long userId, List<DiaryEntity> diaries, List<Long> requestedIds) {
        boolean isAllMine = diaries.stream()
                .allMatch(d -> d.isMyDiary(userId));

        if (diaries.size() != requestedIds.size() || !isAllMine) {
            throw new IllegalArgumentException(INVALID_DIARY_LIST.getDescription());
        }
    }
}
