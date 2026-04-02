package apptive.team5.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserNameUpdateRequest(

        @NotBlank(message = "이름은 비어 있을 수 없습니다.")
        @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z가-힣0-9 ]+$", message = "이름은 영어, 한글, 숫자, 공백만 사용할 수 있습니다.")
        String username

) {
}
