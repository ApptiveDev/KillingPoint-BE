package apptive.team5.user.service;

import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.QUserRepository;
import apptive.team5.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserLowService {

    private final UserRepository userRepository;
    private final QUserRepository qUserRepository;


    @Transactional(readOnly = true)
    public UserEntity findByIdentifier(String identifier) {
        return userRepository.findByIdentifier(identifier)
                .orElseThrow(()-> new NotFoundEntityException(ExceptionCode.NOT_FOUND_USER.getDescription()));
    }

    @Transactional(readOnly = true)
    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new NotFoundEntityException(ExceptionCode.NOT_FOUND_USER.getDescription()));
    }

    @Transactional(readOnly = true)
    public UserEntity getReferenceById(Long id) {
        return userRepository.getReferenceById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByIdentifier(String identifier) {
        return userRepository.findByIdentifier(identifier).isPresent();
    }

    public UserEntity save(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public void deleteByUserId(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsByTag(String tag) {
        return userRepository.findByTag(tag).isPresent();
    }

    @Transactional(readOnly = true)
    public Page<UserEntity> findByTagOrUsernameExcludingBlocked(Set<Long> blockedUserIds, String searchCond, Pageable pageable) {
        return qUserRepository.findByTagOrUsernameExcludingBlocked(blockedUserIds, searchCond,pageable);
    }
}
