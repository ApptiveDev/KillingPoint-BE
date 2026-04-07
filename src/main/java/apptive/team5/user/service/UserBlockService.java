package apptive.team5.user.service;

import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserBlockService {

    private final UserBlockLowService userBlockLowService;
    private final UserLowService userLowService;

    public void addBlockedUser(Long blockedId, Long blockerId) {

        if(userBlockLowService.existsByBlockerIdAndBlockedUserId(blockerId, blockedId))
            throw new DuplicateException(ExceptionCode.DUPLICATE_BLOCKED_USER.getDescription());

        UserEntity blocker = userLowService.getReferenceById(blockerId);

        UserEntity blockedUser = userLowService.findById(blockedId);

        userBlockLowService.save(new UserBlock(blocker, blockedUser));
    }

    public Page<UserResponse> getBlockedUser(Long blockerId, Pageable pageable) {

        return userBlockLowService.findByBlockerIdWithBlockedUser(blockerId, pageable)
                .map(userBlock -> new UserResponse(userBlock.getBlockedUser()));
    }
}
