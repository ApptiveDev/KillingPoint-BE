package apptive.team5.booth.service;

import apptive.team5.booth.dto.PublicKillingPartResponse;
import apptive.team5.booth.dto.PublicUserResponse;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.service.DiaryLowService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class BoothServiceTest {

    @InjectMocks
    private BoothService boothService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private DiaryLowService diaryLowService;

    @Test
    @DisplayName("태그로 유저 조회 - 성공")
    void findUserByTag_success() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        String tag = user.getTag();

        given(userLowService.findByTag(tag)).willReturn(user);

        // when
        PublicUserResponse response = boothService.findUserByTag(tag);

        // then
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.tag()).isEqualTo(tag);
        assertThat(response.profileImageUrl()).contains(user.getProfileImage());

        verify(userLowService).findByTag(tag);
        verifyNoMoreInteractions(userLowService, diaryLowService);
    }

    @Test
    @DisplayName("태그로 유저 조회 - 태그 없으면 NotFoundEntityException")
    void findUserByTag_notFound() {
        // given
        String tag = "MISSING_TAG";
        given(userLowService.findByTag(tag))
                .willThrow(new NotFoundEntityException("존재하지 않는 회원입니다."));

        // when / then
        assertThatThrownBy(() -> boothService.findUserByTag(tag))
                .isInstanceOf(NotFoundEntityException.class);

        verify(userLowService).findByTag(tag);
        verifyNoMoreInteractions(userLowService, diaryLowService);
    }

    @Test
    @DisplayName("플레이리스트 조회 - PUBLIC/KILLING_PART 스코프 필터로 호출")
    void getUserPlaylist_filtersScopes() {
        // given
        UserEntity owner = TestUtil.makeUserEntityWithId();
        Long userId = owner.getId();

        DiaryEntity publicDiary = TestUtil.makeDiaryEntityWithScope(owner, DiaryScope.PUBLIC);
        ReflectionTestUtils.setField(publicDiary, "id", 10L);
        DiaryEntity killingPartDiary = TestUtil.makeDiaryEntityWithScope(owner, DiaryScope.KILLING_PART);
        ReflectionTestUtils.setField(killingPartDiary, "id", 11L);

        Page<DiaryEntity> diaryPage = new PageImpl<>(List.of(publicDiary, killingPartDiary));
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<DiaryScope> visibleScopes = List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);

        given(diaryLowService.findDiaryByUserAndScopeIn(userId, visibleScopes, pageRequest))
                .willReturn(diaryPage);

        // when
        Page<PublicKillingPartResponse> result = boothService.getUserPlaylist(userId, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);

        PublicKillingPartResponse publicDto = result.getContent().get(0);
        assertThat(publicDto.diaryId()).isEqualTo(10L);
        assertThat(publicDto.musicTitle()).isEqualTo("Test Music");
        assertThat(publicDto.artist()).isEqualTo("Test Artist");
        assertThat(publicDto.albumImageUrl()).isEqualTo("image.url");
        assertThat(publicDto.videoUrl()).isEqualTo("video.url");
        assertThat(publicDto.totalDuration()).isEqualTo("PT2M58S");
        assertThat(publicDto.start()).isEqualTo("PT1M1S");
        assertThat(publicDto.end()).isEqualTo("PT1M31S");

        PublicKillingPartResponse killingPartDto = result.getContent().get(1);
        assertThat(killingPartDto.diaryId()).isEqualTo(11L);

        verify(diaryLowService).findDiaryByUserAndScopeIn(userId, visibleScopes, pageRequest);
        verifyNoMoreInteractions(userLowService, diaryLowService);
    }

    @Test
    @DisplayName("플레이리스트 조회 - 다이어리 없으면 빈 페이지")
    void getUserPlaylist_emptyResult() {
        // given
        Long userId = 999L;
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<DiaryScope> visibleScopes = List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);

        given(diaryLowService.findDiaryByUserAndScopeIn(userId, visibleScopes, pageRequest))
                .willReturn(new PageImpl<>(List.of()));

        // when
        Page<PublicKillingPartResponse> result = boothService.getUserPlaylist(userId, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
        verify(diaryLowService).findDiaryByUserAndScopeIn(userId, visibleScopes, pageRequest);
    }
}
