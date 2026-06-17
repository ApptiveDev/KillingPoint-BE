package apptive.team5.admin.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AdminErrorCode {

    NOT_FOUND_ADMIN(HttpStatus.NOT_FOUND, "존재하지 않는 관리자 ID입니다."),
    INVALID_ADMIN_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    AdminErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
