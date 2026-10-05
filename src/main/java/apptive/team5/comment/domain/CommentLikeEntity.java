package apptive.team5.comment.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diary_comment_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_comment_like_user_comment",
                        columnNames = {"user_id", "comment_id"}
                )
        }
)
public class CommentLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_like_user_id_ref_user_id")
    )
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "comment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_like_comment_id_ref_comment_id")
    )
    private CommentEntity comment;

    public CommentLikeEntity(UserEntity user, CommentEntity comment) {
        this.user = user;
        this.comment = comment;
    }
}
