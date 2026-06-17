package apptive.team5.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminLoginRequest {
    @NotBlank(message = "관리자 ID를 입력해주세요.")
    private String adminId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
