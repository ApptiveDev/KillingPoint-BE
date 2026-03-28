package apptive.team5.user.service;

import apptive.team5.user.domain.ClientType;
import apptive.team5.user.domain.ClientVersion;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserInitSettingEntity;
import apptive.team5.user.dto.InitSettingsResponse.AppUpdateStatus;
import apptive.team5.user.util.VersionComparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserInitSettingService {

    private final UserLowService userLowService;
    private final UserInitSettingLowService userInitSettingLowService;

    @Transactional(readOnly = true)
    public boolean checkNeedsTagSetup(Long userId) {
        UserEntity user = userLowService.findById(userId);
        return checkNeedsTagSetup(user);
    }

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

    public void deleteByUserEntity(UserEntity user) {
        userInitSettingLowService.deleteByUserEntity(user);
    }

    @Transactional(readOnly = true)
    public AppUpdateStatus checkAppUpdate(ClientType clientType, String clientVersion) {
        boolean needsForceUpdate = VersionComparator.isLowerThan(clientVersion, ClientVersion.getMinVersion(clientType));
        boolean needsOptionalUpdate = VersionComparator.isLowerThan(clientVersion, ClientVersion.getLatestVersion(clientType));
        return new AppUpdateStatus(needsForceUpdate, needsOptionalUpdate);
    }
}
