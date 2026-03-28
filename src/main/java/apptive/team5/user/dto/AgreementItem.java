package apptive.team5.user.dto;

import apptive.team5.user.domain.PolicyType;
import jakarta.validation.constraints.NotNull;

public record AgreementItem(

        @NotNull(message = "약관 종류는 필수입니다.")
        PolicyType policyType,

        @NotNull(message = "동의 여부는 필수입니다.")
        Boolean agreed
) {
}
