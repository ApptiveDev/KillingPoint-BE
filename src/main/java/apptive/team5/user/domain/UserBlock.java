package apptive.team5.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "unique_user_block",
                columnNames = {
                        "blocker_id",
                        "blocked_to_id"
                }
        ),
}
)
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private UserEntity blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscribed_to_id", nullable = false)
    private UserEntity blockedUser;

    public UserBlock(UserEntity blocker, UserEntity blockedUser) {
        this.blocker = blocker;
        this.blockedUser = blockedUser;
    }
}
