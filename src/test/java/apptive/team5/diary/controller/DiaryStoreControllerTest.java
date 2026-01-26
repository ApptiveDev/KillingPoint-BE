package apptive.team5.diary.controller;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.dto.DiaryLikeResponseDto;
import apptive.team5.diary.dto.DiaryStoreResponseDto;
import apptive.team5.diary.dto.FeedDiaryResponseDto;
import apptive.team5.diary.dto.MyDiaryResponseDto;
import apptive.team5.diary.repository.DiaryLikeRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.diary.repository.DiaryStoreRepository;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiaryStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private DiaryStoreRepository diaryStoreRepository;

    private UserEntity saver;
    private UserEntity userOwner;
    private DiaryEntity diary;

    @BeforeEach
    void setUp() {
        saver = TestUtil.makeUserEntity();
        userRepository.save(saver);

        userOwner = TestUtil.makeDifferentUserEntity(saver);
        userRepository.save(userOwner);

        diary = TestUtil.makeDiaryEntity(userOwner);
        diaryRepository.save(diary);

        TestSecurityContextHolderInjection.inject(saver.getId(), saver.getRoleType());
    }

    @Test
    @DisplayName("저장 추가")
    void toggleDiaryStoreAdd() throws Exception {
        String responseBody = mockMvc.perform(post("/api/diaries/{diaryId}/stores", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DiaryStoreResponseDto responseDto = objectMapper.readValue(responseBody, DiaryStoreResponseDto.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responseDto.isStored()).isTrue();
            softly.assertThat(diaryStoreRepository.existsByUserAndDiary(saver, diary));
        });
    }

    @Test
    @DisplayName("저장 취소")
    void toggleDiaryStoreRemove() throws Exception {
        // given
        diaryStoreRepository.save(new DiaryStoreEntity(saver, diary));

        // when
        String responseBody = mockMvc.perform(post("/api/diaries/{diaryId}/stores", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DiaryStoreResponseDto responseDto = objectMapper.readValue(responseBody, DiaryStoreResponseDto.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responseDto.isStored()).isFalse();
            softly.assertThat(diaryStoreRepository.existsByUserAndDiary(saver, diary));
        });
    }

    @Test
    @DisplayName("저장 실패 - 존재하지 않는 다이어리")
    void toggleDiaryStoreNotFound() throws Exception {
        String responseBody = mockMvc.perform(post("/api/diaries/{diaryId}/stores", 9999L)
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        assertThat(jsonNode.path("message").asText()).isEqualTo("그런 다이어리는 없습니다.");
    }

    @Test
    @DisplayName("저장한 Diary 조회")
    void getStoredDiary() throws Exception {

        DiaryStoreEntity diaryStoreEntity = diaryStoreRepository.save(new DiaryStoreEntity(saver, diary));
        DiaryEntity noneStoreDiary = diaryRepository.save(TestUtil.makeDiaryEntity(userOwner));


        String response = mockMvc.perform(get("/api/diaries/stores")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        JsonNode jsonNode = objectMapper.readTree(response);

        List<FeedDiaryResponseDto> feedDiaryResponseDtos = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<FeedDiaryResponseDto>>() {}
        );

        FeedDiaryResponseDto storedDiary = feedDiaryResponseDtos.getFirst();


        assertSoftly(softly -> {
            softly.assertThat(feedDiaryResponseDtos.size()).isEqualTo(1);
            softly.assertThat(storedDiary.diaryId()).isEqualTo(diary.getId());
        });
    }
  
}
