package apptive.team5.diary.controller;


import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.diary.dto.DiaryReportRequestDto;
import apptive.team5.diary.repository.DiaryReportRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiaryReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiaryReportRepository diaryReportRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("일기 신고 성공")
    @Test
    void reportDiarySuccess() throws Exception {

        UserEntity reportUser = userRepository.save(TestUtil.makeUserEntity());
        UserEntity reportedUser = userRepository.save(TestUtil.makeDifferentUserEntity(reportUser));
        DiaryEntity reportedDiary = diaryRepository.save(TestUtil.makeDiaryEntity(reportedUser));

        DiaryReportRequestDto diaryReportRequestDto = new DiaryReportRequestDto("나쁜말해요");

        TestSecurityContextHolderInjection.inject(reportUser.getId(), reportUser.getRoleType());

        mockMvc.perform(post("/api/diaries/{diaryId}/reports", reportedDiary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diaryReportRequestDto))
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isCreated());

        List<DiaryReportEntity> reportDiaries = diaryReportRepository.findByDiaryId(reportedDiary.getId());

        DiaryReportEntity reportDiariesFirst = reportDiaries.getFirst();

        assertSoftly(softly -> {
            softly.assertThat(reportDiariesFirst.getDiary().getId()).isEqualTo(reportedDiary.getId());
            softly.assertThat(reportDiariesFirst.getReportContent()).isEqualTo(reportedDiary.getContent());
        });

    }


    @DisplayName("일기 신고 실패 - 신고 내용은 1자 이상 200자 이하")
    @ParameterizedTest
    @ValueSource(strings = {"    ", ""})
    void reportDiaryFailDueToContentLimit(String reportContent) throws Exception {

        UserEntity reportUser = userRepository.save(TestUtil.makeUserEntity());
        UserEntity reportedUser = userRepository.save(TestUtil.makeDifferentUserEntity(reportUser));
        DiaryEntity reportedDiary = diaryRepository.save(TestUtil.makeDiaryEntity(reportedUser));

        DiaryReportRequestDto diaryReportRequestDto = new DiaryReportRequestDto(reportContent);

        TestSecurityContextHolderInjection.inject(reportUser.getId(), reportUser.getRoleType());

        mockMvc.perform(post("/api/diaries/{diaryId}/reports", reportedDiary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diaryReportRequestDto))
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isBadRequest());

    }

    @DisplayName("일기 신고 실패 - 이미 신고한 일기")
    @Test
    void reportDiaryFailDueToDuplicate() throws Exception {

        UserEntity reportUser = userRepository.save(TestUtil.makeUserEntity());
        UserEntity reportedUser = userRepository.save(TestUtil.makeDifferentUserEntity(reportUser));
        DiaryEntity reportedDiary = diaryRepository.save(TestUtil.makeDiaryEntity(reportedUser));

        TestSecurityContextHolderInjection.inject(reportUser.getId(), reportUser.getRoleType());

        diaryReportRepository.save(new DiaryReportEntity("나쁜말해요", "나쁜말", reportedDiary, reportUser));

        DiaryReportRequestDto diaryReportRequestDto = new DiaryReportRequestDto("나쁜말 해요");

        TestSecurityContextHolderInjection.inject(reportUser.getId(), reportUser.getRoleType());

        String response = mockMvc.perform(post("/api/diaries/{diaryId}/reports", reportedDiary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diaryReportRequestDto))
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        Map<String, String> apiResponse = objectMapper.readValue(response, new TypeReference<Map<String, String>>() {
        });

        assertSoftly(softly-> {
            softly.assertThat(apiResponse.get("message")).isEqualTo(ExceptionCode.DUPLICATE_DIARY_REPORT.getDescription());
        });

    }

}

