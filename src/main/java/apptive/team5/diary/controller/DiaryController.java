package apptive.team5.diary.controller;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.dto.*;
import apptive.team5.diary.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping("/my")
    public ResponseEntity<Page<MyDiaryResponseDto>> getMyMusicDiary(
            @AuthenticationPrincipal
            Long userId,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "5")
            int size
    ) {

        Page<MyDiaryResponseDto> response = diaryService.getMyDiaries(userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/my/feeds")
    public ResponseEntity<Page<FeedDiaryResponseDto>> getMyUserDiaryFeeds(
            @AuthenticationPrincipal
            Long userId,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "5")
            int size
    ) {

        Page<FeedDiaryResponseDto> response = diaryService.getDiariesFeeds(userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<UserDiaryResponseDto>> getUserDiaries(
            @PathVariable
            Long userId,
            @AuthenticationPrincipal
            Long currentUserId,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "5")
            int size
    ) {
        Page<UserDiaryResponseDto> response = diaryService.getUserDiaries(userId, currentUserId, PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/my/calendar")
    public ResponseEntity<List<CalendarDiaryResponseDto>> getMyDiariesByPeriod(
            @AuthenticationPrincipal
            Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end
    ) {
        List<CalendarDiaryResponseDto> response = diaryService.getMyDiariesByPeriod(userId, start, end);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<Void> createDiary(@AuthenticationPrincipal Long userId, @Valid @RequestBody DiaryCreateRequest diaryRequest) {
        DiaryEntity diary = diaryService.createDiary(userId, diaryRequest);

        return ResponseEntity.status(HttpStatus.CREATED).location(URI.create("/api/diaries/" + diary.getId())).build();
    }

    @PatchMapping("/{diaryId}")
    public ResponseEntity<Void> patchDiary(
            @AuthenticationPrincipal
            Long userId,
            @PathVariable
            Long diaryId,
            @RequestBody
            DiaryUpdateRequestDto updateRequest
    ) {
        diaryService.updateDiary(userId, diaryId, updateRequest);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/randoms")
    public ResponseEntity<List<FeedDiaryResponseDto>> getRandomDiary(
            @AuthenticationPrincipal
            Long userId
    ) {

        List<FeedDiaryResponseDto> randomDiaries = diaryService.getRandomDiaries(userId);

        return ResponseEntity.status(HttpStatus.OK).body(randomDiaries);
    }

    @PutMapping("/{diaryId}")
    public ResponseEntity<Void> putDiary(
            @AuthenticationPrincipal
            Long userId,
            @PathVariable
            Long diaryId,
            @RequestBody
            DiaryUpdateRequestDto updateRequest
    ) {
        diaryService.updateDiary(userId, diaryId, updateRequest);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@AuthenticationPrincipal Long userId, @PathVariable Long diaryId) {
        diaryService.deleteDiary(userId, diaryId);

        return ResponseEntity.noContent().build();
    }
}
