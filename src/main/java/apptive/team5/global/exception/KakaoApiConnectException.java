package apptive.team5.global.exception;

import apptive.team5.oauth2.dto.kakao.KakaoApiExceptionResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class KakaoApiConnectException extends ExternalApiConnectException{

    private final KakaoApiExceptionResponse kakaoApiExceptionResponse;

    public KakaoApiConnectException(String message, HttpStatus httpStatus, KakaoApiExceptionResponse kakaoApiExceptionResponse) {
        super(message, httpStatus);
        this.kakaoApiExceptionResponse = kakaoApiExceptionResponse;
    }
}
