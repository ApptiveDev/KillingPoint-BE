package apptive.team5.user.service;

import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.PolicyRevision;
import apptive.team5.user.domain.PolicyType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserPolicyAgreementEntity;
import apptive.team5.user.dto.PolicyAgreementRequest;
import apptive.team5.user.dto.AgreementItem;
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
