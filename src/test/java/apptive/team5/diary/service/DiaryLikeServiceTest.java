package apptive.team5.diary.service;

import apptive.team5.alarm.service.AlarmDispatchService;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.dto.DiaryLikeResponseDto;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import apptive.team5.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DiaryLikeServiceTest {

    @InjectMocks
    private DiaryLikeService diaryLikeService;

    @Mock
    private DiaryLikeLowService diaryLikeLowService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private DiaryLowService diaryLowService;

    @Mock
    private AlarmDispatchService alarmDispatchService;

    @Test
    @DisplayName("좋아요 토글 - 좋아요 없을 때")
    void toggleDiaryLike_add() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        UserEntity diaryOwner = new UserEntity(2L, "owner-1", "owner@gmail.com", "owner", "ownerTag", user.getRoleType(), user.getSocialType());
        DiaryEntity diary = TestUtil.makeDiaryEntityWithScope(diaryOwner, DiaryScope.KILLING_PART);
        Long userId = user.getId();
        Long diaryId = 1L;

        given(userLowService.getReferenceById(userId)).willReturn(user);
        given(diaryLowService.findDiaryById(diaryId)).willReturn(diary);

        given(diaryLikeLowService.existsByUserAndDiary(user, diary)).willReturn(false);

        // when
        DiaryLikeResponseDto responseDto = diaryLikeService.toggleDiaryLike(userId, diaryId);

        assertSoftly(softly -> {
            softly.assertThat(responseDto.isLiked()).isTrue();
        });

        verify(userLowService).getReferenceById(userId);
        verify(diaryLowService).findDiaryById(diaryId);
        verify(diaryLikeLowService).existsByUserAndDiary(user, diary);
        verify(diaryLikeLowService).saveDiaryLike(any(DiaryLikeEntity.class));
        verifyNoMoreInteractions(userLowService, diaryLowService, diaryLikeLowService, alarmDispatchService);
    }

    @Test
    @DisplayName("좋아요 토글 - 취소")
    void toggleDiaryLike_Remove() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryEntity diary = TestUtil.makeDiaryEntity(user);

        Long userId = user.getId();
        Long diaryId = 1L;
        DiaryLikeEntity diaryLike = new DiaryLikeEntity(user, diary);

        given(userLowService.getReferenceById(userId)).willReturn(user);
        given(diaryLowService.findDiaryById(diaryId)).willReturn(diary);
        given(diaryLikeLowService.existsByUserAndDiary(user, diary)).willReturn(true);
        given(diaryLikeLowService.findByUserAndDiary(user, diary)).willReturn(diaryLike);

        // when
        DiaryLikeResponseDto responseDto = diaryLikeService.toggleDiaryLike(userId, diaryId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responseDto.isLiked()).isFalse();
        });

        verify(userLowService).getReferenceById(userId);
        verify(diaryLowService).findDiaryById(diaryId);
        verify(diaryLikeLowService).existsByUserAndDiary(user, diary);
        verify(diaryLikeLowService).findByUserAndDiary(user, diary);
        verify(diaryLikeLowService).deleteDiaryLike(diaryLike);
        verifyNoInteractions(alarmDispatchService);
        verifyNoMoreInteractions(userLowService, diaryLowService, diaryLikeLowService);
    }
}
