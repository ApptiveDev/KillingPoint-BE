package apptive.team5.user.service;

import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserInitSettingEntity;
import apptive.team5.user.repository.UserInitSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInitSettingLowService {

    private final UserInitSettingRepository initSettingRepository;

    @Transactional(readOnly = true)
    public Optional<UserInitSettingEntity> findByUserEntity(UserEntity user) {
        return initSettingRepository.findByUserEntity(user);
    }

    public UserInitSettingEntity save(UserInitSettingEntity initSetting) {
        return initSettingRepository.save(initSetting);
    }

    public void deleteByUserEntity(UserEntity user) {
        initSettingRepository.deleteByUserEntity(user);
    }
}
