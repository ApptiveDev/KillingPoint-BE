package apptive.team5.diary.controller;

import apptive.team5.alarm.dto.AlarmSendRequest;
import apptive.team5.alarm.entity.AlarmMessage;
import apptive.team5.alarm.service.AlarmDispatchService;
import apptive.team5.diary.dto.DiaryLikeResponseDto;
import apptive.team5.diary.service.DiaryLikeService;
import apptive.team5.user.dto.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/like")
public class DiaryLikeController {

    private final DiaryLikeService diaryLikeService;
    private final AlarmDispatchService alarmDispatchService;

    @PostMapping
    public ResponseEntity<DiaryLikeResponseDto> toggleDiaryLike(
            @AuthenticationPrincipal
            Long userId,
            @PathVariable
            Long diaryId
    ) {
        DiaryLikeResponseDto responseDto = diaryLikeService.toggleDiaryLike(userId, diaryId);

        if (responseDto.isLiked()) alarmDispatchService.saveAndDispatchForLike(new AlarmSendRequest(
                diaryId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<Page<UserSearchResponse>> getDiaryLikeUsers(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String searchCond
    ) {

        Page<UserSearchResponse> response = diaryLikeService.getDiaryLikeUsers(diaryId, userId, searchCond,
                PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
}
