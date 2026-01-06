package apptive.team5.diary.domain;

import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_report_id")
    private Long id;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reportContent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false)
    private DiaryEntity diary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public DiaryReportEntity(String reason, String reportContent, DiaryEntity diary, UserEntity user) {
        this.reason = reason;
        this.reportContent = reportContent;
        this.diary = diary;
        this.user = user;
    }
}
