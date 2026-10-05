package apptive.team5.comment.dto;

import apptive.team5.comment.domain.MentionTargetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MentionReferenceRequest(
        @NotNull(message = "멘션 대상 종류는 필수입니다.")
        MentionTargetType targetType,

        @NotNull(message = "멘션 대상 id는 필수입니다.")
        Long targetId,

        @NotEmpty(message = "멘션 위치(atOrders)는 비어 있을 수 없습니다.")
        List<@NotNull Integer> atOrders,

        @Min(value = 1, message = "멘션 길이(len)는 1 이상이어야 합니다.")
        int len
) {
}
