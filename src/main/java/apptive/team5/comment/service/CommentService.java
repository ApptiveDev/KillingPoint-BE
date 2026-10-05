package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.dto.CommentCreateRequest;
import apptive.team5.comment.dto.CommentPageResponse;
import apptive.team5.comment.dto.CommentResponse;
import apptive.team5.comment.dto.CommentUpdateRequest;
import apptive.team5.comment.mapper.CommentResponseMapper;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserBlockLowService;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentLowService commentLowService;
    private final CommentLikeLowService commentLikeLowService;
    private final DiaryLowService diaryLowService;
    private final UserLowService userLowService;
    private final UserBlockLowService userBlockLowService;
    private final CommentResponseMapper commentResponseMapper;

    @Transactional(readOnly = true)
    public CommentPageResponse getComments(Long diaryId, Long viewerId, Pageable pageable) {
        DiaryEntity diary = diaryLowService.findDiaryById(diaryId);
        validateCommentAccess(diary, viewerId);

        Page<CommentEntity> commentPage = commentLowService.findTopLevelPageOrderByPopularity(diaryId, pageable);
        long commentCount = commentLowService.countActiveTopLevelByDiaryId(diaryId);

        List<Long> commentIds = commentPage.getContent().stream().map(CommentEntity::getId).toList();

        Set<Long> blockedUserIds = userBlockLowService.getBlockedUserIds(viewerId);
        Set<Long> likedCommentIds = commentLikeLowService.findLikedCommentIds(viewerId, commentIds);
        Map<Long, Long> replyCounts = commentLowService.findActiveReplyCounts(commentIds);

        Page<CommentResponse> responsePage = commentResponseMapper.toResponsePage(
                commentPage, viewerId, blockedUserIds, likedCommentIds, replyCounts
        );

        return new CommentPageResponse(commentCount, responsePage);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getReplies(Long parentCommentId, Long viewerId, Pageable pageable) {
        CommentEntity parent = commentLowService.findByIdWithUserAndDiary(parentCommentId);
        validateCommentAccess(parent.getDiary(), viewerId);

        Page<CommentEntity> replyPage = commentLowService.findActiveReplies(parentCommentId, pageable);

        List<Long> replyIds = replyPage.getContent().stream().map(CommentEntity::getId).toList();

        Set<Long> blockedUserIds = userBlockLowService.getBlockedUserIds(viewerId);
        Set<Long> likedCommentIds = commentLikeLowService.findLikedCommentIds(viewerId, replyIds);

        return commentResponseMapper.toResponsePage(replyPage, viewerId, blockedUserIds, likedCommentIds, Map.of());
    }

    public CommentResponse createComment(Long diaryId, Long userId, CommentCreateRequest request) {
        DiaryEntity diary = diaryLowService.findDiaryById(diaryId);
        validateCommentAccess(diary, userId);

        UserEntity user = userLowService.findById(userId);

        CommentEntity parent = null;
        if (request.parentCommentId() != null) {
            parent = commentLowService.findById(request.parentCommentId());
            parent.validateBelongsTo(diaryId);
            parent.validateReplyable();
        }

        CommentEntity saved = commentLowService.save(new CommentEntity(diary, user, parent, request.text()));

        return commentResponseMapper.toResponse(saved, userId, Set.of(), Set.of(), Map.of());
    }

    public CommentResponse updateComment(Long commentId, Long userId, CommentUpdateRequest request) {
        CommentEntity comment = commentLowService.findByIdWithUserAndDiary(commentId);
        comment.validateOwner(userId);
        comment.validateActive();

        comment.edit(request.text());

        Set<Long> likedCommentIds = commentLikeLowService.findLikedCommentIds(userId, List.of(commentId));
        Map<Long, Long> replyCounts = commentLowService.findActiveReplyCounts(List.of(commentId));

        return commentResponseMapper.toResponse(comment, userId, Set.of(), likedCommentIds, replyCounts);
    }

    public void deleteComment(Long commentId, Long userId) {
        CommentEntity comment = commentLowService.findById(commentId);
        comment.validateOwner(userId);
        comment.validateActive();

        comment.delete();
    }

    private void validateCommentAccess(DiaryEntity diary, Long viewerId) {
        if (diary.getScope() == DiaryScope.PRIVATE && !diary.isMyDiary(viewerId)) {
            throw new AccessDeniedException(ExceptionCode.ACCESS_DENIED_DIARY.getDescription());
        }
    }
}
