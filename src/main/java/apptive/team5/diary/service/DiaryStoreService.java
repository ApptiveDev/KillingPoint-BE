package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.dto.DiaryStoreResponseDto;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class DiaryStoreService {

    private final UserLowService userLowService;
    private final DiaryStoreLowService diaryStoreLowService;
    private final DiaryLowService diaryLowService;

    public DiaryStoreResponseDto toggleDiaryStore(Long userId, Long diaryId) {
        UserEntity user = userLowService.getReferenceById(userId);
        DiaryEntity diary = diaryLowService.findDiaryById(diaryId);

        if (diaryStoreLowService.existsByUserAndDiary(user, diary)) {
            DiaryStoreEntity diaryStoreEntity = diaryStoreLowService.findByUserAndDiary(user, diary);
            diaryStoreLowService.deleteById(diaryStoreEntity.getId());
            return new DiaryStoreResponseDto(false);
        }
        else {
            diaryStoreLowService.save(new DiaryStoreEntity(user, diary));
            return new DiaryStoreResponseDto(true);
        }
    }
}
