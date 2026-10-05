package apptive.team5.comment.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diary_comment_report")
public class CommentReportEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_report_id")
    private Long id;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reportContent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "comment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_report_comment_id_ref_comment_id")
    )
    private CommentEntity comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_report_user_id_ref_user_id")
    )
    private UserEntity user;

    public CommentReportEntity(String reason, String reportContent, CommentEntity comment, UserEntity user) {
        this.reason = reason;
        this.reportContent = reportContent;
        this.comment = comment;
        this.user = user;
    }
}
