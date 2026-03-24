package apptive.team5.user.service;

import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserInitSettingEntity;
import apptive.team5.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(SpringExtension.class)
class UserInitSettingServiceTest {

    @InjectMocks
    private UserInitSettingService userInitSettingService;

    @Mock
    private UserInitSettingLowService userInitSettingLowService;

    @Test
    @DisplayName("태그 설정 필요 여부 - 레코드 없으면 false (기존 유저)")
    void checkNeedsTagSetupNoRecord() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        given(userInitSettingLowService.findByUserEntity(user)).willReturn(Optional.empty());

        // when
        boolean result = userInitSettingService.checkNeedsTagSetup(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("태그 설정 필요 여부 - isTagSet=false이면 true (신규 유저, 미설정)")
    void checkNeedsTagSetupNotSet() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        UserInitSettingEntity initSetting = new UserInitSettingEntity(user, false);
        given(userInitSettingLowService.findByUserEntity(user)).willReturn(Optional.of(initSetting));

        // when
        boolean result = userInitSettingService.checkNeedsTagSetup(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("태그 설정 필요 여부 - isTagSet=true이면 false (신규 유저, 설정 완료)")
    void checkNeedsTagSetupAlreadySet() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        UserInitSettingEntity initSetting = new UserInitSettingEntity(user, true);
        given(userInitSettingLowService.findByUserEntity(user)).willReturn(Optional.of(initSetting));

        // when
        boolean result = userInitSettingService.checkNeedsTagSetup(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("초기 설정 생성")
    void createInitSetting() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();

        // when
        userInitSettingService.createInitSetting(user);

        // then
        verify(userInitSettingLowService).save(any(UserInitSettingEntity.class));
    }

    @Test
    @DisplayName("태그 설정 완료 처리")
    void markTagSet() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        UserInitSettingEntity initSetting = new UserInitSettingEntity(user, false);
        given(userInitSettingLowService.findByUserEntity(user)).willReturn(Optional.of(initSetting));

        // when
        userInitSettingService.markTagSet(user);

        // then
        assertThat(initSetting.getIsTagSet()).isTrue();
    }
}
