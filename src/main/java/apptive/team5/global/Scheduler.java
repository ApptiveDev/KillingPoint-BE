package apptive.team5.global;

import apptive.team5.diary.service.DiaryReportService;
import apptive.team5.file.service.S3Service;
import apptive.team5.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class Scheduler {

    private final JwtService jwtService;
    private final S3Service s3Service;
    private final DiaryReportService diaryReportService;

    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredRefreshTokens() {
        jwtService.deleteExpiredRefreshTokens();
    }

    @Scheduled(cron = "0 10 3 * * *")
    public void removeOrphanS3File() {
        s3Service.deleteOrphanS3Files();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void processReportedDiaries() {
        diaryReportService.processReportedDiary();
    }
}
