package apptive.team5.fcm.entity;

import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "device_token",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_device_token_token",
                        columnNames = {"token"}
                )
        }
)
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserEntity user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String token;

    public DeviceToken(UserEntity user, String token) {
        this.user = user;
        this.token = token;
    }

    public void updateMember(UserEntity member) {
        this.user = member;
    }

}
