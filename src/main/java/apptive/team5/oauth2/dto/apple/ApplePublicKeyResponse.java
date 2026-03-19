package apptive.team5.oauth2.dto.apple;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;

import java.util.List;

public record ApplePublicKeyResponse(
        List<ApplePublicKey> keys
) {

    public ApplePublicKey getMatchedKey(String kid, String alg) {
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findAny()
                .orElseThrow(()-> new AuthenticationException(ExceptionCode.APPLE_LOGIN_EXCEPTION.getDescription()));
    }
}
