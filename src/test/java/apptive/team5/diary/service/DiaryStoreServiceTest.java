package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.dto.DiaryStoreResponseDto;
import apptive.team5.recommendation.service.PreferenceService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import apptive.team5.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DiaryStoreServiceTest {

    @InjectMocks
    private DiaryStoreService diaryStoreService;

    @Mock
    private UserLowService userLowService;
    @Mock
    private DiaryStoreLowService diaryStoreLowService;
    @Mock
    private DiaryLowService diaryLowService;
    @Mock
    private PreferenceService preferenceService;

    @Test
    @DisplayName("보관 추가 시 선호도가 반영된다")
    void toggleDiaryStore_add_reflectsPreference() {
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryEntity diary = TestUtil.makeDiaryEntity(user);
        Long userId = user.getId();
        Long diaryId = 1L;

        given(userLowService.getReferenceById(userId)).willReturn(user);
        given(diaryStoreLowService.existsByUserAndDiaryId(user, diaryId)).willReturn(false);
        given(diaryLowService.findDiaryById(diaryId)).willReturn(diary);

        DiaryStoreResponseDto result = diaryStoreService.toggleDiaryStore(userId, diaryId);

        assertThat(result.isStored()).isTrue();
        verify(diaryStoreLowService).save(any(DiaryStoreEntity.class));
        verify(preferenceService).reflectDiaryStored(user, diary);
        verifyNoMoreInteractions(preferenceService);
    }

    @Test
    @DisplayName("보관 취소 시 선호도가 차감된다")
    void toggleDiaryStore_remove_reflectsPreference() {
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryEntity diary = TestUtil.makeDiaryEntity(user);
        Long userId = user.getId();
        Long diaryId = 1L;
        ReflectionTestUtils.setField(diary, "id", diaryId);

        DiaryStoreEntity diaryStoreEntity = new DiaryStoreEntity(user, apptive.team5.diary.domain.model.DiaryStoreInfo.from(diary, user));

        given(userLowService.getReferenceById(userId)).willReturn(user);
        given(diaryStoreLowService.existsByUserAndDiaryId(user, diaryId)).willReturn(true);
        given(diaryStoreLowService.findByUserAndDiaryId(user, diaryId)).willReturn(diaryStoreEntity);
        given(diaryLowService.findDiaryById(diaryId)).willReturn(diary);

        DiaryStoreResponseDto result = diaryStoreService.toggleDiaryStore(userId, diaryId);

        assertThat(result.isStored()).isFalse();
        verify(diaryStoreLowService).deleteById(diaryStoreEntity.getId());
        verify(preferenceService).reflectDiaryStoreRemoved(user, diary);
        verifyNoMoreInteractions(preferenceService);
    }
}
