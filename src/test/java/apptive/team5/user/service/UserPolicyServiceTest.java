package apptive.team5.user.service;

import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.PolicyRevision;
import apptive.team5.user.domain.PolicyType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserPolicyAgreementEntity;
import apptive.team5.user.dto.InitSettingsResponse;
import apptive.team5.user.dto.PolicyAgreementRequest;
import apptive.team5.user.dto.PolicyAgreementRequest.AgreementItem;
import apptive.team5.user.dto.PolicyStatusResponse;
import apptive.team5.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(SpringExtension.class)
class UserPolicyServiceTest {

    @InjectMocks
    private UserPolicyService userPolicyService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private UserPolicyLowService userPolicyLowService;

    @Mock
    private UserInitSettingService userInitSettingService;

    @Test
    @DisplayName("초기 설정 조회 - 약관 동의 기록 없으면 needsUpdate=true")
    void getInitSettingsNeedsUpdate() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();

        given(userLowService.findById(user.getId())).willReturn(user);
        given(userInitSettingService.checkNeedsTagSetup(user)).willReturn(false);
        given(userPolicyLowService.getAgreementMap(user)).willReturn(Map.of());

        // when
        InitSettingsResponse response = userPolicyService.getInitSettings(user.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.needsUpdate()).isTrue();
            softly.assertThat(response.needsTagSetup()).isFalse();
            softly.assertThat(response.policies()).hasSize(PolicyType.values().length);

            for (PolicyStatusResponse policy : response.policies()) {
                softly.assertThat(policy.agreed()).isFalse();
                softly.assertThat(policy.currentRevision()).isEqualTo(0L);
            }
        });
    }

    @Test
    @DisplayName("초기 설정 조회 - 모든 약관 최신 동의 시 needsUpdate=false")
    void getInitSettingsAllAgreed() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();

        Map<PolicyType, UserPolicyAgreementEntity> agreementMap = Map.of(
                PolicyType.SERVICE_TERMS, new UserPolicyAgreementEntity(user, PolicyType.SERVICE_TERMS, true, PolicyRevision.getLatest(PolicyType.SERVICE_TERMS)),
                PolicyType.PRIVACY, new UserPolicyAgreementEntity(user, PolicyType.PRIVACY, true, PolicyRevision.getLatest(PolicyType.PRIVACY))
        );

        given(userLowService.findById(user.getId())).willReturn(user);
        given(userInitSettingService.checkNeedsTagSetup(user)).willReturn(false);
        given(userPolicyLowService.getAgreementMap(user)).willReturn(agreementMap);

        // when
        InitSettingsResponse response = userPolicyService.getInitSettings(user.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.needsUpdate()).isFalse();
            softly.assertThat(response.needsTagSetup()).isFalse();

            for (PolicyStatusResponse policy : response.policies()) {
                softly.assertThat(policy.agreed()).isTrue();
                softly.assertThat(policy.currentRevision()).isEqualTo(policy.latestRevision());
            }
        });
    }

    @Test
    @DisplayName("초기 설정 조회 - 약관 revision이 낡으면 needsUpdate=true")
    void getInitSettingsOutdatedRevision() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();

        Map<PolicyType, UserPolicyAgreementEntity> agreementMap = Map.of(
                PolicyType.SERVICE_TERMS, new UserPolicyAgreementEntity(user, PolicyType.SERVICE_TERMS, true, 0L),
                PolicyType.PRIVACY, new UserPolicyAgreementEntity(user, PolicyType.PRIVACY, true, PolicyRevision.getLatest(PolicyType.PRIVACY))
        );

        given(userLowService.findById(user.getId())).willReturn(user);
        given(userInitSettingService.checkNeedsTagSetup(user)).willReturn(false);
        given(userPolicyLowService.getAgreementMap(user)).willReturn(agreementMap);

        // when
        InitSettingsResponse response = userPolicyService.getInitSettings(user.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.needsUpdate()).isTrue();

            PolicyStatusResponse serviceTerms = response.policies().stream()
                    .filter(p -> p.policyType() == PolicyType.SERVICE_TERMS)
                    .findFirst().orElseThrow();
            softly.assertThat(serviceTerms.currentRevision()).isLessThan(serviceTerms.latestRevision());
        });
    }

    @Test
    @DisplayName("약관 동의 처리 - 정상 동의")
    void agreePolicySuccess() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();

        given(userLowService.findById(user.getId())).willReturn(user);
        given(userPolicyLowService.getAgreementMap(user)).willReturn(Map.of());

        PolicyAgreementRequest request = new PolicyAgreementRequest(List.of(
                new AgreementItem(PolicyType.SERVICE_TERMS, true),
                new AgreementItem(PolicyType.PRIVACY, true)
        ));

        // when
        userPolicyService.agreePolicy(user.getId(), request);

        // then
        verify(userPolicyLowService, org.mockito.Mockito.times(2)).save(any(UserPolicyAgreementEntity.class));
    }

    @Test
    @DisplayName("약관 동의 처리 - 필수 약관 미동의 시 예외")
    void agreePolicyFailRequiredNotAgreed() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();

        given(userLowService.findById(user.getId())).willReturn(user);

        PolicyAgreementRequest request = new PolicyAgreementRequest(List.of(
                new AgreementItem(PolicyType.SERVICE_TERMS, false),
                new AgreementItem(PolicyType.PRIVACY, true)
        ));

        // when & then
        assertThatThrownBy(() -> userPolicyService.agreePolicy(user.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ExceptionCode.REQUIRED_POLICY_NOT_AGREED.getDescription());
    }

    @Test
    @DisplayName("약관 동의 처리 - 기존 동의 기록 업데이트")
    void agreePolicyUpdate() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        UserPolicyAgreementEntity existingAgreement = new UserPolicyAgreementEntity(user, PolicyType.SERVICE_TERMS, true, 0L);

        given(userLowService.findById(user.getId())).willReturn(user);
        given(userPolicyLowService.getAgreementMap(user)).willReturn(Map.of(
                PolicyType.SERVICE_TERMS, existingAgreement
        ));

        PolicyAgreementRequest request = new PolicyAgreementRequest(List.of(
                new AgreementItem(PolicyType.SERVICE_TERMS, true),
                new AgreementItem(PolicyType.PRIVACY, true)
        ));

        // when
        userPolicyService.agreePolicy(user.getId(), request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(existingAgreement.getRevision()).isEqualTo(PolicyRevision.getLatest(PolicyType.SERVICE_TERMS));
            softly.assertThat(existingAgreement.getAgreed()).isTrue();
        });
    }
}
