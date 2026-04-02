package apptive.team5.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserNameUpdateRequest(

        @NotBlank(message = "이름은 비어 있을 수 없습니다.")
        @Size(min = 4, max = 10, message = "이름은 4자 이상 10자 미만이어야 합니다.")
        @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 영어 또는 한글만 사용할 수 있습니다.")
        String username

) {
}
