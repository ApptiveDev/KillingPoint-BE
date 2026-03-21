package apptive.team5.oauth2.component;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.oauth2.dto.apple.ApplePublicKey;
import apptive.team5.oauth2.dto.apple.ApplePublicKeyResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class AppleKeyGenerator {

    @Value("${apple.keyId}")
    private String kid;
    @Value("${apple.teamId}")
    private String teamId;
    @Value("${apple.clientId}")
    private String clientId;
    @Value("${apple.privateKey}")
    private String privateKey;


    public PublicKey generatePublicKey(Map<String, String> tokenHeaders,
                                       ApplePublicKeyResponse applePublicKeys)  {
        try {
            ApplePublicKey publicKey = applePublicKeys.getMatchedKey(tokenHeaders.get("kid"),
                    tokenHeaders.get("alg"));
            return getPublicKey(publicKey);
        } catch (Exception ex) {
            log.info("apple public key generation failed");
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

    public String getClientSecret() {
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .header().keyId(kid).add("alg", "ES256").and()
                .issuer(teamId)
                .issuedAt(new Date())
                .expiration(expirationDate)
                .audience().add("https://appleid.apple.com").and()
                .subject(clientId)
                .signWith(getPrivateKey(), Jwts.SIG.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            Reader pemReader = new StringReader(privateKey.replace("\\n", "\n"));
            PEMParser pemParser = new PEMParser(pemReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo object = (PrivateKeyInfo)pemParser.readObject();
            PrivateKey parsedKey = converter.getPrivateKey(object);
            log.info("Apple private key parsed successfully - algorithm: {}", parsedKey.getAlgorithm());
            return parsedKey;
        } catch (IOException e) {
            log.info("getPrivateKey IOException");
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }
    }


}
