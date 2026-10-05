package apptive.team5.comment.domain;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diary_comment",
        indexes = {
                @Index(name = "idx_comment_diary_parent_status", columnList = "diary_id, parent_id, status"),
                @Index(name = "idx_comment_parent_status", columnList = "parent_id, status"),
                @Index(name = "idx_comment_user", columnList = "user_id")
        }
)
public class CommentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "diary_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_diary_id_ref_diary_id")
    )
    private DiaryEntity diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_comment_user_id_ref_user_id")
    )
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            foreignKey = @ForeignKey(name = "fk_comment_parent_id_ref_comment_id")
    )
    private CommentEntity parent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long likeCount;

    private LocalDateTime editedDateTime;

    public CommentEntity(DiaryEntity diary, UserEntity user, CommentEntity parent, String content) {
        this.diary = diary;
        this.user = user;
        this.parent = parent;
        this.content = content;
        this.status = CommentStatus.ACTIVE;
        this.likeCount = 0L;
    }

    public boolean isReply() {
        return this.parent != null;
    }

    public boolean isActive() {
        return this.status == CommentStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == CommentStatus.DELETED;
    }

    public boolean isEdited() {
        return this.editedDateTime != null;
    }

    public boolean isMine(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    public Long getParentId() {
        return this.parent == null ? null : this.parent.getId();
    }

    public void validateOwner(Long userId) {
        if (!isMine(userId)) {
            throw new AccessDeniedException(ExceptionCode.ACCESS_DENIED_COMMENT.getDescription());
        }
    }

    public void validateActive() {
        if (!isActive()) {
            throw new BadRequestException(ExceptionCode.DELETED_COMMENT.getDescription());
        }
    }

    public void validateReplyable() {
        if (isReply() || !isActive()) {
            throw new BadRequestException(ExceptionCode.INVALID_COMMENT_PARENT.getDescription());
        }
    }

    public void validateBelongsTo(Long diaryId) {
        if (!this.diary.getId().equals(diaryId)) {
            throw new BadRequestException(ExceptionCode.INVALID_COMMENT_PARENT.getDescription());
        }
    }

    public void edit(String content) {
        this.content = content;
        this.editedDateTime = LocalDateTime.now();
    }

    public void delete() {
        this.status = CommentStatus.DELETED;
    }

    public void detachUser() {
        this.user = null;
    }
}
