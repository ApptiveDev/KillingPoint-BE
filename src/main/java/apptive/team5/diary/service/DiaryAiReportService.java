package apptive.team5.diary.service;

import apptive.team5.diary.dto.AiDiaryReportRequestDto;
import apptive.team5.diary.dto.AiDiaryReportResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryAiReportService {

    private final DiaryReportLowService diaryReportLowService;
    private final ChatClient chatClient;
    @Value("${diary.report.prompt}")
    private String reportPrompt;

    public Optional<List<Long>> getInvalidDiaryIds() {
        List<String> reportedList = diaryReportLowService.findAll()
                .stream()
                .map(diaryReportEntity -> new AiDiaryReportRequestDto(diaryReportEntity).toString()).toList();

        if (reportedList.isEmpty()) return Optional.empty();

        String reportRequest = String.join("\n", reportedList);

        Prompt prompt = makeDiaryReportPrompt(reportRequest);

        List<Long> invalidDiaryIds = chatClient.prompt(prompt)
                .call().entity(AiDiaryReportResponseDto.class).diaryIds().stream().toList();

        return Optional.of(invalidDiaryIds);
    }

    private Prompt makeDiaryReportPrompt(String reportRequest) {
        SystemMessage systemMessage = new SystemMessage(reportPrompt);

        UserMessage userMessage = new UserMessage(reportRequest);

        return new Prompt(systemMessage, userMessage);
    }
}
