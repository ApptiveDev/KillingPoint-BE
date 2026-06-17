package apptive.team5.admin.exception;

import lombok.Getter;

@Getter
public class AdminException extends RuntimeException {

    private final AdminErrorCode errorCode;

    public AdminException(AdminErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
