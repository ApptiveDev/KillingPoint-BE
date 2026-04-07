package apptive.team5.user.repository;

import apptive.team5.user.domain.UserBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock,Long> {

    @Query("select ub from UserBlock ub where ub.blocker.id = :blockerId and ub.blockedUser.id = :blockedUserId")
    Optional<UserBlock> findByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);

    @Query("select ub from UserBlock ub join fetch ub.blockedUser where ub.blocker.id = :blockerId")
    Page<UserBlock> findByBlockerIdWithBlockedUser(Long blockerId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserBlock ub where ub.blockedUser.id = :userId or ub.blocker.id = :userId")
    void deleteByUserId(Long userId);
}
