package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.dto.DiaryLikeResponseDto;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryLikeService {
    private final DiaryLikeLowService diaryLikeLowService;
    private final UserLowService userLowService;
    private final DiaryLowService diaryLowService;

    public DiaryLikeResponseDto toggleDiaryLike(Long userId, Long diaryId) {
        UserEntity user = userLowService.getReferenceById(userId);
        DiaryEntity diary = diaryLowService.findDiaryById(diaryId);

        if (diaryLikeLowService.existsByUserAndDiary(user, diary)) {
            DiaryLikeEntity diaryLike = diaryLikeLowService.findByUserAndDiary(user, diary);
            diaryLikeLowService.deleteDiaryLike(diaryLike);
            return new DiaryLikeResponseDto(false);
        }
        else {
            diaryLikeLowService.saveDiaryLike(new DiaryLikeEntity(user, diary));
            return new DiaryLikeResponseDto(true);
        }
    }
}
