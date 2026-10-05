package apptive.team5.comment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diary_comment_mention",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_comment_mention_comment_at_order",
                        columnNames = {"comment_id", "atOrder"}
                )
        },
        indexes = {
                @Index(name = "idx_comment_mention_target", columnList = "targetType, targetId")
        }
)
public class CommentMentionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_mention_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "comment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_mention_comment_id_ref_comment_id")
    )
    private CommentEntity comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MentionTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private int atOrder;

    public CommentMentionEntity(CommentEntity comment, MentionTargetType targetType, Long targetId, int atOrder) {
        this.comment = comment;
        this.targetType = targetType;
        this.targetId = targetId;
        this.atOrder = atOrder;
    }
}
