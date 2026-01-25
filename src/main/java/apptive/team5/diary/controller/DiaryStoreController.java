package apptive.team5.diary.controller;

import apptive.team5.diary.dto.DiaryStoreResponseDto;
import apptive.team5.diary.service.DiaryStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryStoreController {

    private final DiaryStoreService diaryStoreService;

    @PostMapping("/{diaryId}/stores")
    public ResponseEntity<DiaryStoreResponseDto> toggleDiaryStore(
            @AuthenticationPrincipal
            Long userId,
            @PathVariable
            Long diaryId
    ) {
        DiaryStoreResponseDto diaryStoreResponseDto = diaryStoreService.toggleDiaryStore(userId, diaryId);

        return ResponseEntity.status(HttpStatus.OK).body(diaryStoreResponseDto);
    }
}
