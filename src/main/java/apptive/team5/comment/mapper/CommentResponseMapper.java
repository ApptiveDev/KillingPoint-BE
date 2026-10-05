package apptive.team5.comment.mapper;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.dto.CommentAuthorResponse;
import apptive.team5.comment.dto.CommentResponse;
import apptive.team5.user.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CommentResponseMapper {

    public Page<CommentResponse> toResponsePage(
            Page<CommentEntity> comments,
            Long viewerId,
            Set<Long> blockedUserIds,
            Set<Long> likedCommentIds,
            Map<Long, Long> replyCounts
    ) {
        return comments.map(comment -> toResponse(comment, viewerId, blockedUserIds, likedCommentIds, replyCounts));
    }

    public List<CommentResponse> toResponseList(
            List<CommentEntity> comments,
            Long viewerId,
            Set<Long> blockedUserIds,
            Set<Long> likedCommentIds,
            Map<Long, Long> replyCounts
    ) {
        return comments.stream()
                .map(comment -> toResponse(comment, viewerId, blockedUserIds, likedCommentIds, replyCounts))
                .toList();
    }

    public CommentResponse toResponse(
            CommentEntity comment,
            Long viewerId,
            Set<Long> blockedUserIds,
            Set<Long> likedCommentIds,
            Map<Long, Long> replyCounts
    ) {
        UserEntity author = comment.getUser();
        long replyCount = replyCounts.getOrDefault(comment.getId(), 0L);

        if (comment.isDeleted()) {
            return masked(comment, false, null, replyCount);
        }

        boolean isBlocked = author != null && blockedUserIds.contains(author.getId());
        if (isBlocked) {
            return masked(comment, true, CommentAuthorResponse.blocked(author), replyCount);
        }

        return new CommentResponse(
                comment.getId(),
                comment.getDiary().getId(),
                comment.getParentId(),
                comment.getStatus(),
                false,
                comment.getContent(),
                author == null ? null : CommentAuthorResponse.from(author),
                comment.isMine(viewerId),
                likedCommentIds.contains(comment.getId()),
                comment.getLikeCount(),
                replyCount,
                comment.getCreateDateTime(),
                comment.isEdited()
        );
    }

    private CommentResponse masked(CommentEntity comment, boolean isBlocked, CommentAuthorResponse author, long replyCount) {
        return new CommentResponse(
                comment.getId(),
                comment.getDiary().getId(),
                comment.getParentId(),
                comment.getStatus(),
                isBlocked,
                null,
                author,
                false,
                false,
                0L,
                replyCount,
                comment.getCreateDateTime(),
                comment.isEdited()
        );
    }
}
