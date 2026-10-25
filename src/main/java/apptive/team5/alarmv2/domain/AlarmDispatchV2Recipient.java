package apptive.team5.alarmv2.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "alarm_dispatch_v2_recipient", indexes = {
        @Index(name = "idx_alarm_dispatch_v2_recipient_dispatch", columnList = "dispatch_id"),
        @Index(name = "idx_alarm_dispatch_v2_recipient_user", columnList = "recipient_user_id")
})
public class AlarmDispatchV2Recipient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private AlarmDispatchV2 dispatch;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    public AlarmDispatchV2Recipient(AlarmDispatchV2 dispatch, Long recipientUserId) {
        this.dispatch = dispatch;
        this.recipientUserId = recipientUserId;
    }
}
