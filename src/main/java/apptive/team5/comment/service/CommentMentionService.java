package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentMentionEntity;
import apptive.team5.comment.domain.MentionTargetType;
import apptive.team5.comment.dto.CommentAttachmentResponse;
import apptive.team5.comment.dto.MentionDisplayResponse;
import apptive.team5.comment.dto.MentionReferenceRequest;
import apptive.team5.comment.dto.MentionCandidateResponse;
import apptive.team5.comment.dto.MentionReferenceResponse;
import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.subscribe.service.SubscribeLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserBlockLowService;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentMentionService {

    private static final int MAX_MENTION_TARGETS = 5;
    private static final int MAX_CANDIDATE_SIZE = 20;
    private static final char AT_SIGN = '@';

    private final CommentMentionLowService commentMentionLowService;
    private final UserLowService userLowService;
    private final UserBlockLowService userBlockLowService;
    private final SubscribeLowService subscribeLowService;

    public void createMentions(CommentEntity comment, String text, List<MentionReferenceRequest> references, Long authorId) {
        if (references.isEmpty()) {
            return;
        }

        validate(text, references, authorId);

        List<CommentMentionEntity> mentions = new ArrayList<>();
        for (MentionReferenceRequest reference : references) {
            for (Integer atOrder : reference.atOrders()) {
                mentions.add(new CommentMentionEntity(comment, reference.targetType(), reference.targetId(), atOrder, reference.len()));
            }
        }
        commentMentionLowService.saveAll(mentions);
    }

    public void replaceMentions(CommentEntity comment, String text, List<MentionReferenceRequest> references, Long authorId) {
        commentMentionLowService.deleteByCommentIds(List.of(comment.getId()));
        createMentions(comment, text, references, authorId);
    }

    @Transactional(readOnly = true)
    public List<MentionCandidateResponse> getMentionCandidates(Long userId, String keyword, int size) {
        int limit = Math.max(1, Math.min(size, MAX_CANDIDATE_SIZE));
        Set<Long> blockedUserIds = userBlockLowService.getBlockedUserIds(userId);

        return subscribeLowService.findSubscribedUsersByKeyword(userId, blockedUserIds, keyword, limit)
                .stream()
                .map(MentionCandidateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, CommentAttachmentResponse> getAttachments(List<Long> commentIds) {
        List<CommentMentionEntity> mentions = commentMentionLowService.findByCommentIds(commentIds);
        if (mentions.isEmpty()) {
            return Map.of();
        }

        Set<Long> userIds = mentions.stream()
                .filter(mention -> mention.getTargetType() == MentionTargetType.USER)
                .map(CommentMentionEntity::getTargetId)
                .collect(Collectors.toSet());
        Map<Long, UserEntity> users = userLowService.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        Map<Long, Map<MentionKey, List<CommentMentionEntity>>> grouped = new LinkedHashMap<>();
        for (CommentMentionEntity mention : mentions) {
            grouped.computeIfAbsent(mention.getComment().getId(), id -> new LinkedHashMap<>())
                    .computeIfAbsent(new MentionKey(mention.getTargetType(), mention.getTargetId()), key -> new ArrayList<>())
                    .add(mention);
        }

        Map<Long, CommentAttachmentResponse> result = new LinkedHashMap<>();
        grouped.forEach((commentId, byTarget) -> {
            List<MentionReferenceResponse> references = byTarget.entrySet().stream()
                    .map(entry -> toReference(entry.getKey(), entry.getValue(), users))
                    .toList();
            result.put(commentId, new CommentAttachmentResponse(references));
        });
        return result;
    }

    private MentionReferenceResponse toReference(MentionKey key, List<CommentMentionEntity> rows, Map<Long, UserEntity> users) {
        List<Integer> atOrders = rows.stream().map(CommentMentionEntity::getAtOrder).sorted().toList();
        UserEntity user = key.targetType() == MentionTargetType.USER ? users.get(key.targetId()) : null;

        return new MentionReferenceResponse(
                key.targetType(),
                key.targetId(),
                atOrders,
                rows.getFirst().getLength(),
                user == null ? null : MentionDisplayResponse.from(user)
        );
    }

    private void validate(String text, List<MentionReferenceRequest> references, Long authorId) {
        List<Integer> atPositions = atSignPositions(text);
        Set<Integer> usedAtOrders = new HashSet<>();
        Set<Long> targetIds = new HashSet<>();

        for (MentionReferenceRequest reference : references) {
            if (reference.targetType() != MentionTargetType.USER) {
                throw new BadRequestException(ExceptionCode.UNSUPPORTED_MENTION_TARGET.getDescription());
            }

            for (Integer atOrder : reference.atOrders()) {
                if (atOrder < 1 || atOrder > atPositions.size() || !usedAtOrders.add(atOrder)) {
                    throw new BadRequestException(ExceptionCode.INVALID_MENTION_REFERENCE.getDescription());
                }
                int mentionEnd = atPositions.get(atOrder - 1) + 1 + reference.len();
                if (mentionEnd > text.length()) {
                    throw new BadRequestException(ExceptionCode.INVALID_MENTION_REFERENCE.getDescription());
                }
            }

            targetIds.add(reference.targetId());
        }

        if (targetIds.size() > MAX_MENTION_TARGETS) {
            throw new BadRequestException(ExceptionCode.MENTION_LIMIT_EXCEEDED.getDescription());
        }

        if (userLowService.findAllByIds(targetIds).size() != targetIds.size()) {
            throw new NotFoundEntityException(ExceptionCode.NOT_FOUND_USER.getDescription());
        }

        Set<Long> blockedUserIds = userBlockLowService.getBlockedUserIds(authorId);
        if (targetIds.stream().anyMatch(blockedUserIds::contains)) {
            throw new BadRequestException(ExceptionCode.BLOCKED_MENTION_TARGET.getDescription());
        }
    }

    private List<Integer> atSignPositions(String text) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == AT_SIGN) {
                positions.add(i);
            }
        }
        return positions;
    }

    private record MentionKey(MentionTargetType targetType, Long targetId) {
    }
}
