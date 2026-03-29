package apptive.team5.user.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInitSettingEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity userEntity;

    @Column(nullable = false)
    private Boolean isTagSet;

    public UserInitSettingEntity(UserEntity userEntity, boolean isTagSet) {
        this.userEntity = userEntity;
        this.isTagSet = isTagSet;
    }

    public void markTagSet() {
        this.isTagSet = true;
    }
}
