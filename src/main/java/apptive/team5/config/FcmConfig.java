package apptive.team5.config;

import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.ExternalApiConnectException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

@Configuration
@Slf4j
public class FcmConfig {

    @Value("${fcm.firebase.config.path}")
    private String firebaseConfigPath;

    @Bean
    public GoogleCredentials googleCredentials() {
        try {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            return googleCredentials;
        } catch (IOException ex) {
            log.error("Failed to load Firebase credentials from classpath: {}", firebaseConfigPath, ex);
            throw new ExternalApiConnectException("firebase 연결 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Bean
    public FirebaseApp firebaseApp(GoogleCredentials googleCredentials) {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(googleCredentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }


}
