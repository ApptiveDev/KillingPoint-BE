package apptive.team5.mail.service;

import apptive.team5.comment.dto.CommentReportResponseDto;
import apptive.team5.diary.dto.DiaryReportResponseDto;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.survey.email}")
    private String surveySubscribeEmail;
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async("sendMail")
    public void sendSurveyMailMessage(String content) {

        try {

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(surveySubscribeEmail);
            helper.setSubject("KillingPart 설문조사가 도착했습니다.");
            helper.setFrom(senderEmail, "KillingPart");
            helper.setText(setContext(content), true);

            javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Async("sendMail")
    public void sendReportedMailMessage(DiaryReportResponseDto diaryReportResponseDto) {
        sendReportMail(
                "게시글 신고가 접수되었습니다.",
                "\uD83D\uDCCC 다이어리 신고가 접수되었습니다",
                diaryReportResponseDto.id(),
                diaryReportResponseDto.reason(),
                diaryReportResponseDto.reportContent(),
                diaryReportResponseDto.userId()
        );
    }

    @Async("sendMail")
    public void sendCommentReportedMailMessage(CommentReportResponseDto commentReportResponseDto) {
        sendReportMail(
                "댓글 신고가 접수되었습니다.",
                "\uD83D\uDCCC 댓글 신고가 접수되었습니다 (댓글 ID " + commentReportResponseDto.commentId() + ")",
                commentReportResponseDto.id(),
                commentReportResponseDto.reason(),
                commentReportResponseDto.reportContent(),
                commentReportResponseDto.userId()
        );
    }

    private void sendReportMail(String subject, String title, Long reportId, String reason, String reportContent, Long userId) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(surveySubscribeEmail);
            helper.setSubject(subject);
            helper.setFrom(senderEmail, "KillingPart");
            helper.setText(setReportContext(title, reportId, reason, reportContent, userId), true);

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String setReportContext(String title, Long reportId, String reason, String reportContent, Long userId) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("reportId", reportId);
        context.setVariable("reason", reason);
        context.setVariable("reportContent", reportContent);
        context.setVariable("userId", userId);
        return templateEngine.process("reportedForm", context);
    }

    private String setContext(String content) {

        Context context = new Context();
        context.setVariable("content", content);
        return templateEngine.process("survey", context);

    }
}


