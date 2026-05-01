package apptive.team5.diary.service;

import apptive.team5.alarm.dto.AlarmSendRequest;
import apptive.team5.alarm.service.AlarmDispatchService;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.dto.DiaryLikeResponseDto;
import apptive.team5.subscribe.service.SubscribeLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.dto.UserSearchResponse;
import apptive.team5.user.service.UserBlockLowService;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryLikeService {

    private final DiaryLikeLowService diaryLikeLowService;
    private final UserLowService userLowService;
    private final DiaryLowService diaryLowService;
    private final SubscribeLowService subscribeLowService;
    private final UserBlockLowService userBlockLowService;

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

    @Transactional(readOnly = true)
    public Page<UserSearchResponse> getDiaryLikeUsers(Long diaryId, Long userId, String searchCond, Pageable pageable) {

        DiaryEntity findDiary = diaryLowService.findDiaryById(diaryId);

        Set<Long> blockedUserIds = userBlockLowService.getBlockedUserIds(userId);

        // 다이어리 좋야요를 누른 사용자 중 차단되지 않은 유저
        Page<UserEntity> likeUsers = diaryLikeLowService.findByDiaryIdLikeSearchCondExcludedBlockedUsers(diaryId, searchCond, blockedUserIds, pageable)
                .map(DiaryLikeEntity::getUser);

        List<Long> diaryLikeUserIds = likeUsers.stream().map(UserEntity::getId).toList();

        // 현재 로그인한 사용자가 구독한 Id
        Set<Long> mySubscribedIds = subscribeLowService.findBySubscriberIdAndSubscribedToIds(userId, diaryLikeUserIds)
                .stream()
                .map(subscribe -> subscribe.getSubscribedTo().getId()).collect(Collectors.toSet());

        return likeUsers
                .map(user -> {
                    boolean isMyPick = mySubscribedIds.contains(user.getId());
                    return new UserSearchResponse(user, isMyPick);
                });
    }
}
