package apptive.team5.diary.controller;

import apptive.team5.diary.dto.DiaryOrderUpdateRequestDto;
import apptive.team5.diary.service.DiaryOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries/order")
public class DiaryOrderController {
    private final DiaryOrderService diaryOrderService;

    @PatchMapping
    public ResponseEntity<Void> updateDiaryOrder(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DiaryOrderUpdateRequestDto requestDto
    ) {
        diaryOrderService.updateDiaryOrder(userId, requestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
