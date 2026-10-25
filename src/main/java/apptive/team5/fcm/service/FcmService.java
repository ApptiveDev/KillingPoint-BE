package apptive.team5.fcm.service;

import apptive.team5.alarm.entity.AlarmMessage;
import apptive.team5.fcm.dto.DeviceTokenRequest;
import apptive.team5.fcm.entity.DeviceToken;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
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

    public void deleteDeviceTokenByUserId(Long userId) {

        deviceTokenLowService.deleteByUserId(userId);
    }

    @Async("sendAlarm")
    public void sendAlarm(Long userId, AlarmMessage alarmMessage, String content, String deepLink) {

        UserEntity user = userLowService.findById(userId);
        if (!user.isAlarmEnabled()) {
            return;
        }

        List<DeviceToken> deviceTokens = deviceTokenLowService.findByUserId(userId);
        if (deviceTokens.isEmpty()) {
            return;
        }

        deviceTokens.forEach(deviceToken -> sendMessageTo(userId, deviceToken.getToken(), alarmMessage, content, deepLink));
    }

    @Async("sendAlarm")
    public void sendAdminAlarmV2(Long userId, String title, String content, String deepLink) {
        UserEntity user = userLowService.findById(userId);
        if (!user.isAlarmEnabled()) return;

        List<DeviceToken> deviceTokens = deviceTokenLowService.findByUserId(userId);
        deviceTokens.forEach(deviceToken -> sendMessageTo(userId, deviceToken.getToken(), title, content, deepLink, AlarmMessage.ADMIN_ALARM.name()));
    }

    private void sendMessageTo(Long userId, String targetToken, AlarmMessage alarmMessage, String body, String deepLink) {

        sendMessageTo(userId, targetToken, alarmMessage.getMessage(), body, deepLink, alarmMessage.name());
    }

    private void sendMessageTo(Long userId, String targetToken, String title, String body, String deepLink, String type) {

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .putData("deepLink", deepLink)
                .putData("type", type)
                .build();

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        ApiFuture<String> future = firebaseMessaging.sendAsync(message);
        future.addListener(() -> handleSendResult(future, userId, targetToken), Runnable::run);
    }

    private void handleSendResult(ApiFuture<String> future, Long userId, String targetToken) {
        try {
            String messageId = future.get();
            log.info("FCM 전송 성공: userId={}, tokenHash={}, messageId={}", userId, tokenHash(targetToken), messageId);
            return;
        } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof FirebaseMessagingException fme) {
                log.error("FCM 전송 실패: userId={}, tokenHash={}, httpStatus={}, messagingErrorCode={}, firebaseErrorCode={}, message={}",
                        userId, tokenHash(targetToken), fme.getHttpResponse() == null ? null : fme.getHttpResponse().getStatusCode(),
                        fme.getMessagingErrorCode(), fme.getErrorCode(), fme.getMessage(), ex);
            } else {
                log.error("FCM 전송 실패(FCM 상세 예외 아님): userId={}, tokenHash={}, cause={}",
                        userId, tokenHash(targetToken), cause == null ? ex.getMessage() : cause.toString(), ex);
            }
            if (!(cause instanceof FirebaseMessagingException fme)) {
                return;
            }
            if (fme.getMessagingErrorCode() != MessagingErrorCode.UNREGISTERED) {
                return;
            }
        }

        deviceTokenLowService.deleteByToken(targetToken);
    }

    private String tokenHash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 6);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
