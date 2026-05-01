package apptive.team5.fcm.service;

import apptive.team5.fcm.dto.DeviceTokenRequest;
import apptive.team5.fcm.entity.DeviceToken;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class FcmService {

    private final DeviceTokenLowService deviceTokenLowService;
    private final UserLowService userLowService;

    public DeviceToken addDeviceToken(Long userId, DeviceTokenRequest deviceTokenRequest) {
        Optional<DeviceToken> deviceToken = deviceTokenLowService.findByToken(deviceTokenRequest.token());

        UserEntity user = userLowService.findById(userId);


        if (deviceToken.isPresent()) {
            deviceToken.get().updateUser(user);
            return deviceToken.get();
        }

        return deviceTokenLowService.save(new DeviceToken(user, deviceTokenRequest.token()));
    }

    @Async("sendAlarm")
    public void sendAlarm(Long userId, String title, String content, String deepLink) {

        List<DeviceToken> deviceTokens = deviceTokenLowService.findByUserId(userId);


        deviceTokens.forEach(deviceToken -> {
            sendMessageTo(deviceToken.getToken(), title, content, deepLink);
        });

    }

    private void sendMessageTo(String targetToken, String title, String body, String deepLink) {

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .putData("deepLink", deepLink)
                .build();

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        ApiFuture<String> future = firebaseMessaging.sendAsync(message);

        future.addListener(() -> {
            try {
                future.get();
            } catch (Exception ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof FirebaseMessagingException fme) {
                    if (fme.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                        deviceTokenLowService.deleteByToken(targetToken);
                    }
                }
            }
        }, Runnable::run);
    }
}
