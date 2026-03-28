package apptive.team5.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PolicyAgreementRequest(

        @Valid
        @NotNull(message = "약관 동의 목록은 필수입니다.")
        List<AgreementItem> agreements
) {
}
