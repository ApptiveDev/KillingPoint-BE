package apptive.team5.diary.controller;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.dto.DiaryLikeResponseDto;
import apptive.team5.diary.dto.RandomDiaryResponseDto;
import apptive.team5.diary.repository.DiaryLikeRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.repository.SubscribeRepository;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.dto.UserSearchResponse;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DiaryLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private DiaryLikeRepository diaryLikeRepository;
    @Autowired
    private SubscribeRepository subscribeRepository;

    private UserEntity userLiker;
    private UserEntity userOwner;
    private DiaryEntity diary;

    @BeforeEach
    void setUp() {
        userLiker = TestUtil.makeUserEntity();
        userRepository.save(userLiker);

        userOwner = TestUtil.makeDifferentUserEntity(userLiker);
        userRepository.save(userOwner);

        diary = TestUtil.makeDiaryEntity(userOwner);
        diaryRepository.save(diary);

        TestSecurityContextHolderInjection.inject(userLiker.getId(), userLiker.getRoleType());
    }

    @Test
    @DisplayName("좋아요 추가")
    void toggleDiaryLikeAdd() throws Exception {
        String responseBody = mockMvc.perform(post("/api/diaries/{diaryId}/like", diary.getId())
                .with(securityContext(SecurityContextHolder.getContext()))
        )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DiaryLikeResponseDto responseDto = objectMapper.readValue(responseBody, DiaryLikeResponseDto.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responseDto.isLiked()).isTrue();
            softly.assertThat(diaryLikeRepository.existsByUserAndDiary(userLiker, diary));
        });
    }

    @Test
    @DisplayName("좋아요 취소")
    void toggleDiaryLikeRemove() throws Exception {
        // given
        diaryLikeRepository.save(new DiaryLikeEntity(userLiker, diary));

        // when
        String responseBody = mockMvc.perform(post("/api/diaries/{diaryId}/like", diary.getId())
                .with(securityContext(SecurityContextHolder.getContext()))
        )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DiaryLikeResponseDto responseDto = objectMapper.readValue(responseBody, DiaryLikeResponseDto.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responseDto.isLiked()).isFalse();
            softly.assertThat(diaryLikeRepository.existsByUserAndDiary(userLiker, diary));
        });
    }

    @Test
    @DisplayName("좋아요 실패 - 존재하지 않는 다이어리")
    void toggleDiaryLikeNotFound() throws Exception {
        String responseBody = mockMvc.perform(post("/api/diaries/{diaryId}/like", 9999L)
                .with(securityContext(SecurityContextHolder.getContext()))
        )
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        assertThat(jsonNode.path("message").asText()).isEqualTo("그런 다이어리는 없습니다.");
    }

    @Test
    @DisplayName("좋아요한 사람 조회")
    void getDiaryLikeUsers() throws Exception {
        // given
        diaryLikeRepository.save(new DiaryLikeEntity(userLiker, diary));
        UserEntity secondLikeUser = userRepository.save(new UserEntity("1234", "1234", "1234", "1234", UserRoleType.USER, SocialType.KAKAO));
        diaryLikeRepository.save(new DiaryLikeEntity(secondLikeUser, diary));

        subscribeRepository.save(new Subscribe(userLiker, secondLikeUser));

        // when
        String response = mockMvc.perform(get("/api/diaries/{diaryId}/like", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        List<UserSearchResponse> content = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<UserSearchResponse>>() {}
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(content.size()).isEqualTo(2);
            softly.assertThat(content.getLast().userId()).isEqualTo(userLiker.getId());
            softly.assertThat(content.getFirst().userId()).isEqualTo(secondLikeUser.getId());
            softly.assertThat(content.getFirst().isMyPick()).isTrue();
        });
    }

    @Test
    @DisplayName("좋아요한 사람 조회 with 유저 검색")
    void getDiaryLikeUsersWithSearchCond() throws Exception {
        // given
        diaryLikeRepository.save(new DiaryLikeEntity(userLiker, diary));
        UserEntity secondLikeUser = userRepository.save(new UserEntity("1234", "1234", "1234", "1234", UserRoleType.USER, SocialType.KAKAO));
        diaryLikeRepository.save(new DiaryLikeEntity(secondLikeUser, diary));

        // when
        String response = mockMvc.perform(get("/api/diaries/{diaryId}/like?searchCond={username}", diary.getId(), userLiker.getUsername())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        List<UserResponse> content = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<UserResponse>>() {}
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(content.size()).isEqualTo(1);
            softly.assertThat(content.getFirst().userId()).isEqualTo(userLiker.getId());
        });
    }
}
