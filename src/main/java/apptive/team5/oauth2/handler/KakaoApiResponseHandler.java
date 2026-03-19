package apptive.team5.oauth2.handler;

import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.ExternalApiConnectException;
import apptive.team5.global.exception.KakaoApiConnectException;
import apptive.team5.oauth2.dto.kakao.KakaoApiExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

public class KakaoApiResponseHandler implements ResponseErrorHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        ResponseErrorHandler.super.handleError(url, method, response);
        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());

        KakaoApiExceptionResponse apiExceptionResponse;
        try {
            apiExceptionResponse = objectMapper.readValue(response.getBody(), KakaoApiExceptionResponse.class);
        } catch (IOException e) {
            throw new ExternalApiConnectException(ExceptionCode.KAKAO_API_EXCEPTION.getDescription() + "[예외 응답 파싱 실패]", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (apiExceptionResponse.code() == -401) {
            throw new ExternalApiConnectException(ExceptionCode.KAKAO_API_EXCEPTION.getDescription()+ "[액세스 토큰 만료]",  HttpStatus.UNAUTHORIZED);
        }

        throw new KakaoApiConnectException(ExceptionCode.KAKAO_API_EXCEPTION.getDescription(), httpStatus, apiExceptionResponse);
    }
}
