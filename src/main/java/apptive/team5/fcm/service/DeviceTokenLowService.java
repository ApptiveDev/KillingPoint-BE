package apptive.team5.fcm.service;


import apptive.team5.fcm.entity.DeviceToken;
import apptive.team5.fcm.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceTokenLowService {


    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceToken save(DeviceToken deviceToken) {
        return deviceTokenRepository.save(deviceToken);
    }

    public void deleteByUserId(Long userId) {
        deviceTokenRepository.deleteByUserId(userId);
    }

    public List<DeviceToken> findByUserId(Long userId) {
        return deviceTokenRepository.findByUserId(userId);
    }

    public Optional<DeviceToken> findByToken(String token) {
        return deviceTokenRepository.findByToken(token);
    }

    public void deleteByToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
