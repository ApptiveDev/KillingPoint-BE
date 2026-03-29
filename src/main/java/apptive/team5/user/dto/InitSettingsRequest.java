package apptive.team5.user.dto;

import apptive.team5.user.domain.ClientType;
import jakarta.validation.constraints.NotNull;

public record InitSettingsRequest(

        @NotNull(message = "클라이언트 타입은 필수입니다.")
        ClientType clientType,

        @NotNull(message = "클라이언트 버전은 필수입니다.")
        String clientVersion
) {
}
