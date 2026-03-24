package apptive.team5.user.service;

import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserInitSettingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserInitSettingService {

    private final UserInitSettingLowService userInitSettingLowService;

    @Transactional(readOnly = true)
    public boolean checkNeedsTagSetup(UserEntity user) {
        return userInitSettingLowService.findByUserEntity(user)
                .map(setting -> !setting.getIsTagSet())
                .orElse(false);
    }

    public void createInitSetting(UserEntity user) {
        userInitSettingLowService.save(new UserInitSettingEntity(user, false));
    }

    public void markTagSet(UserEntity user) {
        userInitSettingLowService.findByUserEntity(user)
                .ifPresent(UserInitSettingEntity::markTagSet);
    }
}
