package apptive.team5.diary.controller;

import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.dto.DiaryReportRequestDto;
import apptive.team5.diary.dto.DiaryReportResponseDto;
import apptive.team5.diary.service.DiaryReportService;
import apptive.team5.mail.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryReportController {

    private final DiaryReportService diaryReportService;
    private final MailService mailService;

    @PostMapping("/{diaryId}/reports")
    public ResponseEntity<Void> reportDiary(@Valid @RequestBody DiaryReportRequestDto diaryReportRequestDto, @PathVariable Long diaryId,
                                            @AuthenticationPrincipal Long userId) {

        DiaryReportResponseDto diaryReportResponseDto = diaryReportService.createDiaryReport(diaryReportRequestDto, diaryId, userId);
        mailService.sendReportedMailMessage(diaryReportResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
