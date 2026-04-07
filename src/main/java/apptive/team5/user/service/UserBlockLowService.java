package apptive.team5.user.service;

import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserBlockLowService {

    private final UserBlockRepository userBlockRepository;

    public UserBlock save(UserBlock userBlock) {
        return userBlockRepository.save(userBlock);
    }

    public boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId) {
        if (userBlockRepository.findByBlockerIdAndBlockedUserId(blockerId, blockedUserId).isPresent()) return true;
        return false;
    }

    public Page<UserBlock> findByBlockerIdWithBlockedUser(Long blockerId, Pageable pageable) {
        return userBlockRepository.findByBlockerIdWithBlockedUser(blockerId, pageable);
    }

    public void deleteByUserId(Long userId) {
        userBlockRepository.deleteByUserId(userId);
    }
}
