package apptive.team5.comment.controller;

import apptive.team5.comment.dto.CommentLikeResponse;
import apptive.team5.comment.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/{commentId}/like")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping
    public ResponseEntity<CommentLikeResponse> toggleCommentLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId
    ) {
        CommentLikeResponse response = commentLikeService.toggleCommentLike(userId, commentId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
