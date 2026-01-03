package apptive.team5.subscribe.domain;

import apptive.team5.user.domain.UserEntity;
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
                        name = "unique_subscribe",
                        columnNames = {
                                "subscriber_id",
                                "subscribed_to_id"
                        }
                ),
        }
)
public class Subscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false) // 구독자
    private UserEntity subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscribed_to_id", nullable = false) // 구독대상
    private UserEntity subscribedTo;

    public Subscribe(UserEntity subscriber, UserEntity subscribedTo) {
        this.subscriber = subscriber;
        this.subscribedTo = subscribedTo;
    }
}
