package apptive.team5.fcm.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
        @NotBlank
        String token
) {
}
