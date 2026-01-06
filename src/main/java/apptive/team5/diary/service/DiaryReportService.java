package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.dto.AiDiaryReportRequestDto;
import apptive.team5.diary.dto.AiDiaryReportResponseDto;
import apptive.team5.diary.dto.DiaryReportRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryReportService {

    private final DiaryReportLowService diaryReportLowService;
    private final DiaryLowService diaryLowService;
    private final DiaryOrderLowService diaryOrderLowService;
    private final DiaryLikeLowService diaryLikeLowService;
    private final ChatClient chatClient;
    @Value("${diary.report.prompt}")
    private String reportPrompt;

    public DiaryReportEntity createDiaryReport(DiaryReportRequestDto diaryReportRequestDto, Long diaryId) {

        DiaryEntity reportedDiary = diaryLowService.findDiaryById(diaryId);

        return diaryReportLowService.save(new DiaryReportEntity(diaryReportRequestDto.content(), reportedDiary.getContent(), reportedDiary));
    }

    public void processReportedDiary() {

        List<String> reportedList = diaryReportLowService.findAll()
                .stream()
                .map(diaryReportEntity -> new AiDiaryReportRequestDto(diaryReportEntity).toString()).toList();

        if (reportedList.isEmpty()) return;


        String reportRequest = String.join("\n", reportedList);

        SystemMessage systemMessage = new SystemMessage(reportPrompt);

        UserMessage userMessage = new UserMessage(reportRequest);

        Prompt prompt = new Prompt(systemMessage, userMessage);

        Set<Long> response = chatClient.prompt(prompt)
                .call().entity(AiDiaryReportResponseDto.class).diaryIds();

        ArrayList<Long> invalidDiaryIds = new ArrayList<>(response);

        if (invalidDiaryIds.isEmpty()) return;

        System.out.println(invalidDiaryIds);

        diaryReportLowService.deleteAllWithBulk();
        diaryLikeLowService.deleteByDiaryIds(invalidDiaryIds);

        Set<Long> userIds = diaryLowService.findAllByIds(invalidDiaryIds)
                .stream()
                .map(diary -> diary.getUser().getId())
                .collect(Collectors.toSet());

        diaryOrderLowService.deleteByDiaryIds(userIds, invalidDiaryIds);

        diaryLowService.deleteByDiaryIds(invalidDiaryIds);
    }
}
