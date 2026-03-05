package apptive.team5.diary.controller;

import apptive.team5.diary.dto.DiaryLikeResponseDto;
import apptive.team5.diary.service.DiaryLikeService;
import apptive.team5.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/like")
public class DiaryLikeController {

    private final DiaryLikeService diaryLikeService;

    @PostMapping
    public ResponseEntity<DiaryLikeResponseDto> toggleDiaryLike(
            @AuthenticationPrincipal
            Long userId,
            @PathVariable
            Long diaryId
    ) {
        DiaryLikeResponseDto responseDto = diaryLikeService.toggleDiaryLike(userId, diaryId);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getDiaryLikeUsers(
            @PathVariable Long diaryId,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "100")
            int size
    ) {

        Page<UserResponse> response = diaryLikeService.getDiaryLikeUsers(diaryId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
}
