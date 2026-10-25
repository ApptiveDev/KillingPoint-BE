package apptive.team5.alarmv2.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 기존 Alarm 엔티티 및 FCM 발송 흐름과 분리된 관리자 알림 v2의 발송 단위다.
 * 관리자 알림 v2의 발송 요청과 대상 기준을 기록한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "alarm_dispatch_v2",
        indexes = {
                @Index(name = "idx_alarm_dispatch_v2_status_scheduled", columnList = "status, scheduled_at"),
                @Index(name = "idx_alarm_dispatch_v2_created", columnList = "create_date_time")
        }
)
public class AlarmDispatchV2 extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, length = 1000)
    private String deepLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlarmDispatchV2RecipientMode recipientMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmDispatchV2Type dispatchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlarmDispatchV2Status status;

    @Column(nullable = false)
    private int recipientCount;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    public AlarmDispatchV2(String title, String content, String deepLink,
                           AlarmDispatchV2RecipientMode recipientMode,
                           AlarmDispatchV2Type dispatchType,
                           AlarmDispatchV2Status status,
                           int recipientCount,
                           LocalDateTime scheduledAt) {
        this.title = title;
        this.content = content;
        this.deepLink = deepLink;
        this.recipientMode = recipientMode;
        this.dispatchType = dispatchType;
        this.status = status;
        this.recipientCount = recipientCount;
        this.scheduledAt = scheduledAt;
    }

    public void changeStatus(AlarmDispatchV2Status status) {
        this.status = status;
    }
}
