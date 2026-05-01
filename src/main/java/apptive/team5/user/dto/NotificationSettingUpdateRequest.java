package apptive.team5.user.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingUpdateRequest(
        @NotNull(message = "알림 수신 여부는 필수입니다.")
        Boolean alarmEnabled
) {
}
