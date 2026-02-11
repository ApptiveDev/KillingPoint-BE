package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.domain.model.DiaryStoreInfo;
import apptive.team5.diary.dto.DiaryStoreResponseDto;
import apptive.team5.diary.dto.FeedDiaryResponseDto;
import apptive.team5.diary.dto.StoredDiaryResponseDto;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        if (diaryStoreLowService.existsByUserAndDiaryId(user, diaryId)) {
            DiaryStoreEntity diaryStoreEntity = diaryStoreLowService.findByUserAndDiaryId(user, diaryId);
            diaryStoreLowService.deleteById(diaryStoreEntity.getId());
            return new DiaryStoreResponseDto(false);
        }
        else {
            DiaryEntity diary = diaryLowService.findDiaryById(diaryId);
            DiaryStoreInfo storeInfo = DiaryStoreInfo.from(diary, user);
            diaryStoreLowService.save(new DiaryStoreEntity(user, storeInfo));
            return new DiaryStoreResponseDto(true);
        }
    }

    @Transactional(readOnly = true)
    public Page<StoredDiaryResponseDto> getStoredDiaries(Long userId, Pageable pageable) {

        Page<DiaryStoreEntity> diaryStoredPage = diaryStoreLowService.findStoredDiaryByUser(userId, pageable);

        return diaryStoredPage.map(StoredDiaryResponseDto::from);
    }
}
