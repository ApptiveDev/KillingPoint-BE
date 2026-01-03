package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.dto.DiaryCreateRequest;
import apptive.team5.diary.dto.MyDiaryResponseDto;
import apptive.team5.diary.dto.DiaryUpdateRequestDto;
import apptive.team5.diary.dto.UserDiaryResponseDto;
import apptive.team5.diary.mapper.DiaryResponseMapper;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.service.UserLowService;
import apptive.team5.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {
    @Spy
    DiaryResponseMapper diaryResponseMapper;

    @InjectMocks
    private DiaryService diaryService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private DiaryLowService diaryLowService;
    @Mock
    private DiaryLikeLowService diaryLikeLowService;
    @Mock
    private DiaryOrderLowService diaryOrderLowService;

    @Test
    @DisplayName("내 다이어리 목록 조회 - diaryOrder 없는 상황")
    void getMyDiaries() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryEntity diary = TestUtil.makeDiaryEntity(user);
        Page<DiaryEntity> diaryEntityPage = new PageImpl<>(List.of(diary));
        PageRequest pageRequest = PageRequest.of(0, 5);

        given(userLowService.getReferenceById(user.getId())).willReturn(user);
        given(diaryOrderLowService.findByUserId(user.getId())).willReturn(Optional.empty());
        given(diaryLowService.findDiaryByUser(user, pageRequest)).willReturn(diaryEntityPage);

        // when
        Page<MyDiaryResponseDto> result = diaryService.getMyDiaries(user.getId(), pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).musicTitle()).isEqualTo("Test Music");
        verify(userLowService).getReferenceById(any(Long.class));
        verify(diaryLowService).findDiaryByUser(any(UserEntity.class), any(PageRequest.class));

        verifyNoMoreInteractions(userLowService, diaryLowService);
    }

    @Test
    @DisplayName("타인 다이어리 조회 - PUBLIC, KILLING_PART")
    void getUserDiaries_OtherUser() {
        // given
        UserEntity owner = TestUtil.makeUserEntityWithId();

        UserEntity viewer = new UserEntity(
                2L, "viewer-id", "viewer@email.com", "viewer",
                "viewerTag", UserRoleType.USER, SocialType.GOOGLE
        );

        DiaryEntity publicDiary = TestUtil.makeDiaryEntityWithScope(owner, DiaryScope.PUBLIC);
        ReflectionTestUtils.setField(publicDiary, "id", 10L);
        DiaryEntity killingPartDiary = TestUtil.makeDiaryEntityWithScope(owner, DiaryScope.KILLING_PART);
        ReflectionTestUtils.setField(killingPartDiary, "id", 11L);

        List<DiaryEntity> diaries = List.of(publicDiary, killingPartDiary);
        Page<DiaryEntity> diaryPage = new PageImpl<>(diaries);
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<DiaryScope> visibleScopes = List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);

        Set<Long> likedDiaryIds = Set.of(publicDiary.getId());

        given(diaryLowService.findDiaryByUserAndScopeIn(owner.getId(), visibleScopes, pageRequest)).willReturn(diaryPage);
        given(diaryLikeLowService.findLikedDiaryIdsByUser(eq(viewer.getId()), any(List.class))).willReturn(likedDiaryIds);

        // when
        Page<UserDiaryResponseDto> result = diaryService.getUserDiaries(owner.getId(), viewer.getId(), pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);

        UserDiaryResponseDto publicDto = result.getContent().get(0);
        assertThat(publicDto.scope()).isEqualTo(DiaryScope.PUBLIC);
        assertThat(publicDto.totalDuration()).isEqualTo(publicDiary.getTotalDuration());
        assertThat(publicDto.content()).isEqualTo(publicDiary.getContent());
        assertThat(publicDto.isLiked()).isTrue();

        UserDiaryResponseDto killingPartDto = result.getContent().get(1);
        assertThat(killingPartDto.scope()).isEqualTo(DiaryScope.KILLING_PART);
        assertThat(killingPartDto.totalDuration()).isEqualTo(killingPartDiary.getTotalDuration());
        assertThat(killingPartDto.content()).isEqualTo("비공개 일기입니다.");
        assertThat(killingPartDto.isLiked()).isFalse();

        verify(diaryLowService).findDiaryByUserAndScopeIn(owner.getId(), visibleScopes, pageRequest);
        verify(diaryLikeLowService).findLikedDiaryIdsByUser(eq(viewer.getId()), any(List.class));
        verify(userLowService, never()).getReferenceById(any());
    }

    @Test
    @DisplayName("내 다이어리 조회 (getUserDiaries 사용)")
    void getUserDiaries_OwnUser() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        Long userId = user.getId();

        DiaryEntity privateDiary = TestUtil.makeDiaryEntityWithScope(user, DiaryScope.PRIVATE);
        ReflectionTestUtils.setField(privateDiary, "id", 10L);
        DiaryEntity killingPartDiary = TestUtil.makeDiaryEntityWithScope(user, DiaryScope.KILLING_PART);
        ReflectionTestUtils.setField(killingPartDiary, "id", 11L);

        List<DiaryEntity> diaries = List.of(privateDiary, killingPartDiary);
        Page<DiaryEntity> diaryPage = new PageImpl<>(diaries);
        PageRequest pageRequest = PageRequest.of(0, 5);

        Set<Long> likedDiaryIds = Set.of();

        given(userLowService.getReferenceById(userId)).willReturn(user);
        given(diaryLowService.findDiaryByUser(user, pageRequest)).willReturn(diaryPage);
        given(diaryLikeLowService.findLikedDiaryIdsByUser(eq(userId), any(List.class))).willReturn(likedDiaryIds);

        Page<UserDiaryResponseDto> result = diaryService.getUserDiaries(userId, userId, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);

        UserDiaryResponseDto privateDto = result.getContent().getFirst();
        assertThat(privateDto.scope()).isEqualTo(DiaryScope.PRIVATE);
        assertThat(privateDto.content()).isEqualTo(privateDiary.getContent());
        assertThat(privateDto.isLiked()).isFalse();

        UserDiaryResponseDto killingPartDto = result.getContent().get(1);
        assertThat(killingPartDto.scope()).isEqualTo(DiaryScope.KILLING_PART);
        assertThat(killingPartDto.content()).isEqualTo(killingPartDiary.getContent());
        assertThat(killingPartDto.isLiked()).isFalse();

        verify(userLowService).getReferenceById(userId);
        verify(diaryLowService).findDiaryByUser(user, pageRequest);
        verify(diaryLikeLowService).findLikedDiaryIdsByUser(eq(userId), any(List.class));
        verify(diaryLowService, never()).findDiaryByUserAndScopeIn(any(), any(), any());
    }

    @Test
    @DisplayName("다이어리 생성")
    void createDiary() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryCreateRequest diaryRequest = TestUtil.makeDiaryCreateRequest();

        given(userLowService.getReferenceById(user.getId())).willReturn(user);

        // when
        diaryService.createDiary(user.getId(), diaryRequest);

        // then
        verify(userLowService).getReferenceById(any(Long.class));
        verify(diaryLowService).saveDiary(any(DiaryEntity.class));

        verifyNoMoreInteractions(userLowService, diaryLowService);
    }

    @Test
    @DisplayName("다이어리 수정")
    void updateDiary() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        Long diaryId = 1L;
        DiaryUpdateRequestDto updateRequest = TestUtil.makeDiaryUpdateRequest();
        DiaryEntity diary = TestUtil.makeDiaryEntity(user);

        given(userLowService.getReferenceById(user.getId())).willReturn(user);
        given(diaryLowService.findDiaryById(diaryId)).willReturn(diary);


        // when
        diaryService.updateDiary(user.getId(), diaryId, updateRequest);

        // then
        verify(userLowService).getReferenceById(any(Long.class));
        verify(diaryLowService).findDiaryById(any(Long.class));
        verify(diaryLowService).updateDiary(any(DiaryEntity.class), any());

        verifyNoMoreInteractions(userLowService, diaryLowService);
    }

    @Test
    @DisplayName("다이어리 삭제")
    void deleteDiary() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        Long diaryId = 1L;
        DiaryEntity diary = TestUtil.makeDiaryEntityWithId(diaryId, user);

        given(userLowService.getReferenceById(user.getId())).willReturn(user);
        given(diaryLowService.findDiaryById(diaryId)).willReturn(diary);

        // when
        diaryService.deleteDiary(user.getId(), diaryId);

        // then
        verify(userLowService).getReferenceById(any(Long.class));
        verify(diaryLowService).findDiaryById(any(Long.class));
        verify(diaryLowService).deleteDiary(any(DiaryEntity.class));

        verifyNoMoreInteractions(userLowService, diaryLowService);
    }
}
