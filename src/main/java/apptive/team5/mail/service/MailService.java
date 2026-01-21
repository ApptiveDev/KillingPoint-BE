package apptive.team5.mail.service;

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

        try {

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(surveySubscribeEmail);
            helper.setSubject("게시글 신고가 접수되었습니다.");
            helper.setFrom(senderEmail, "KillingPart");
            helper.setText(setReportContext(diaryReportResponseDto), true);

            javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String setReportContext(DiaryReportResponseDto diaryReportResponseDto) {
        Context context = new Context();
        context.setVariable("reportId", diaryReportResponseDto.id());
        context.setVariable("reason", diaryReportResponseDto.reason());
        context.setVariable("reportContent", diaryReportResponseDto.reportContent());
        context.setVariable("userId", diaryReportResponseDto.userId());
        return templateEngine.process("reportedForm", context);
    }

    private String setContext(String content) {

        Context context = new Context();
        context.setVariable("content", content);
        return templateEngine.process("survey", context);

    }
}


