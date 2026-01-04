package apptive.team5.diary.controller;

import apptive.team5.diary.dto.DiaryReportRequestDto;
import apptive.team5.diary.service.DiaryReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryReportController {

    private final DiaryReportService diaryReportService;

    @PostMapping("/{diaryId}/reports")
    public ResponseEntity<Void> reportDiary(@Valid @RequestBody DiaryReportRequestDto diaryReportRequestDto, @PathVariable Long diaryId) {

        diaryReportService.createDiaryReport(diaryReportRequestDto, diaryId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
