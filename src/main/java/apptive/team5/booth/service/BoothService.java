package apptive.team5.booth.service;

import apptive.team5.booth.dto.PublicKillingPartResponse;
import apptive.team5.booth.dto.PublicUserResponse;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class BoothService {

    private final UserLowService userLowService;
    private final DiaryLowService diaryLowService;

    private static final List<DiaryScope> VISIBLE_SCOPES =
            List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);

    @Transactional(readOnly = true)
    public PublicUserResponse findUserByTag(String tag) {
        UserEntity user = userLowService.findByTag(tag);
        return PublicUserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<PublicKillingPartResponse> getUserPlaylist(Long userId, Pageable pageable) {
        Page<DiaryEntity> diaryPage =
                diaryLowService.findDiaryByUserAndScopeIn(userId, VISIBLE_SCOPES, pageable);

        return diaryPage.map(PublicKillingPartResponse::from);
    }
}
