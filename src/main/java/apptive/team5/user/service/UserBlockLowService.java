package apptive.team5.user.service;

import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class UserBlockLowService {

    private final UserBlockRepository userBlockRepository;

    public UserBlock save(UserBlock userBlock) {
        return userBlockRepository.save(userBlock);
    }

    @Transactional(readOnly = true)
    public boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId) {
        if (userBlockRepository.findByBlockerIdAndBlockedUserId(blockerId, blockedUserId).isPresent()) return true;
        return false;
    }

    @Transactional(readOnly = true)
    public Page<UserBlock> findByBlockerIdWithBlockedUser(Long blockerId, Pageable pageable) {
        return userBlockRepository.findByBlockerIdWithBlockedUser(blockerId, pageable);
    }

    @Transactional(readOnly = true)
    public Set<Long> getBlockedUserIds(Long userId) {
        return userBlockRepository.findByUserId(userId)
                .stream().map(userBlock -> {
                    if (userBlock.getBlockedUser().getId().equals(userId)) return userBlock.getBlocker().getId();
                    return userBlock.getBlockedUser().getId();
                }).collect(Collectors.toSet());
    }

    public void deleteByUserId(Long userId) {
        userBlockRepository.deleteByUserId(userId);
    }
}
