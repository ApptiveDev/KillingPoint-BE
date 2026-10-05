package apptive.team5.comment.dto;

import apptive.team5.comment.domain.MentionTargetType;

import java.util.List;

public record MentionReferenceResponse(
        MentionTargetType targetType,
        Long targetId,
        List<Integer> atOrders,
        int len,
        MentionDisplayResponse display
) {
}
