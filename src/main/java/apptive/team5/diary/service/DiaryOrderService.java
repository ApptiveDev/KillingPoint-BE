package apptive.team5.diary.service;

import apptive.team5.diary.dto.DiaryOrderUpdateRequestDto;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryOrderService {
    private final DiaryOrderLowService diaryOrderLowService;
    private final UserLowService userLowService;

    public void updateDiaryOrder(Long userId, DiaryOrderUpdateRequestDto requestDto) {
        UserEntity user = userLowService.getReferenceById(userId);

        diaryOrderLowService.saveOrder(user, requestDto.diaryIds());
    }
}
