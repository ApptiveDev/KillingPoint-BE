package apptive.team5.oauth2.component;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.oauth2.dto.apple.ApplePublicKey;
import apptive.team5.oauth2.dto.apple.ApplePublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppleKeyGenerator {
    public PublicKey generatePublicKey(Map<String, String> tokenHeaders,
                                       ApplePublicKeyResponse applePublicKeys)  {
        try {
            ApplePublicKey publicKey = applePublicKeys.getMatchedKey(tokenHeaders.get("kid"),
                    tokenHeaders.get("alg"));
            return getPublicKey(publicKey);
        } catch (Exception ex) {
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }

    }

    private PublicKey getPublicKey(ApplePublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
        byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes),
                new BigInteger(1, eBytes));
        KeyFactory keyFactory = KeyFactory.getInstance(publicKey.kty());
        return keyFactory.generatePublic(publicKeySpec);
    }
}
