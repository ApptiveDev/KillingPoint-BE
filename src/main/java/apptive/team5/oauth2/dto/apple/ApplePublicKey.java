package apptive.team5.oauth2.dto.apple;

public record ApplePublicKey(String kty,
                             String kid,
                             String alg,
                             String n,
                             String e) {
}
