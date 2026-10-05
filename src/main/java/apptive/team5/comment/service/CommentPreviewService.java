package apptive.team5.comment.service;

import apptive.team5.comment.dto.CommentPreviewItem;
import apptive.team5.comment.dto.CommentPreviewResponse;
import apptive.team5.user.service.UserBlockLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentPreviewService {

    private static final int PREVIEW_SIZE = 3;

    private final CommentLowService commentLowService;
    private final UserBlockLowService userBlockLowService;

    public Map<Long, CommentPreviewResponse> getPreviews(List<Long> diaryIds, Long viewerId) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> commentCounts = commentLowService.countActiveTopLevelByDiaryIds(diaryIds);
        if (commentCounts.isEmpty()) {
            return Map.of();
        }

        Set<Long> blockedUserIds = userBlockLowService.getBlockedUserIds(viewerId);

        Map<Long, CommentPreviewResponse> previews = new LinkedHashMap<>();
        for (Long diaryId : diaryIds) {
            long commentCount = commentCounts.getOrDefault(diaryId, 0L);
            if (commentCount == 0L) {
                continue;
            }

            List<CommentPreviewItem> items = commentLowService.findRecentActiveTopLevel(diaryId, blockedUserIds, PREVIEW_SIZE)
                    .stream()
                    .map(CommentPreviewItem::from)
                    .toList();

            previews.put(diaryId, new CommentPreviewResponse(commentCount, items));
        }
        return previews;
    }

    public CommentPreviewResponse getPreview(Long diaryId, Long viewerId) {
        return getPreviews(List.of(diaryId), viewerId).getOrDefault(diaryId, CommentPreviewResponse.empty());
    }
}
