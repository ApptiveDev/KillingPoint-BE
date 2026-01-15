package apptive.team5.global;

import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.service.DiaryAiReportService;
import apptive.team5.diary.service.DiaryReportLowService;
import apptive.team5.diary.service.DiaryReportService;
import apptive.team5.file.service.S3Service;
import apptive.team5.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class Scheduler {

    private final JwtService jwtService;
    private final S3Service s3Service;
    private final DiaryReportService diaryReportService;
    private final DiaryReportLowService diaryReportLowService;
    private final DiaryAiReportService diaryAiReportService;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRefreshTokens() {
        jwtService.deleteExpiredRefreshTokens();
    }

    @Scheduled(cron = "0 10 3 * * *")
    public void removeOrphanS3File() {
        s3Service.deleteOrphanS3Files();
    }

//    @Scheduled(cron = "0 0 * * * *")
//    public void processReportedDiaries() {
//        List<DiaryReportEntity> recentTop10DiaryReport = diaryReportLowService.findRecentTop10DiaryReport();
//        Optional<List<Long>> invalidDiaryIds = diaryAiReportService.getInvalidDiaryIds(recentTop10DiaryReport);
//
//        if (invalidDiaryIds.isPresent())
//            diaryReportService.processReportedDiary(invalidDiaryIds.get(), recentTop10DiaryReport);
//
//    }
}
