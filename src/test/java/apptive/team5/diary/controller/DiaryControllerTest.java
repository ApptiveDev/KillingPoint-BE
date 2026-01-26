package apptive.team5.diary.controller;

import apptive.team5.diary.domain.*;
import apptive.team5.diary.dto.*;
import apptive.team5.diary.repository.DiaryReportRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.diary.repository.DiaryStoreRepository;
import apptive.team5.diary.service.DiaryLikeLowService;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.repository.SubscribeRepository;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private DiaryLowService diaryLowService;

    @Autowired
    private DiaryLikeLowService diaryLikeLowService;

    @Autowired
    private DiaryReportRepository  diaryReportRepository;

    @Autowired
    private DiaryStoreRepository diaryStoreRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = TestUtil.makeUserEntity();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("내 다이어리 목록 조회")
    void getMyMusicDiary() throws Exception {
        // given
        DiaryEntity firstDiary = diaryRepository.save(TestUtil.makeDiaryEntity(testUser));
        DiaryEntity secondDiary = diaryRepository.save(TestUtil.makeDiaryEntity(testUser));
        diaryLikeLowService.saveDiaryLike(new DiaryLikeEntity(testUser, secondDiary)); // 두번째 다이어리에 좋아요

        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        // when & then
        String response = mockMvc.perform(get("/api/diaries/my")
                        .param("page", "0")
                        .param("size", "5")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode jsonNode = objectMapper.readTree(response);

        List<MyDiaryResponseDto> content = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<MyDiaryResponseDto>>() {}
        );

        MyDiaryResponseDto diaryResponse = content.getFirst();

        assertSoftly(softly-> {
            softly.assertThat(content).hasSize(2);
            softly.assertThat(diaryResponse.diaryId()).isEqualTo(secondDiary.getId());
            softly.assertThat(diaryResponse.musicTitle()).isEqualTo(secondDiary.getMusicTitle());
            softly.assertThat(diaryResponse.artist()).isEqualTo(secondDiary.getArtist());
            softly.assertThat(diaryResponse.duration()).isEqualTo(secondDiary.getDuration());
            softly.assertThat(diaryResponse.totalDuration()).isEqualTo(secondDiary.getTotalDuration());
            softly.assertThat(diaryResponse.start()).isEqualTo(secondDiary.getStart());
            softly.assertThat(diaryResponse.end()).isEqualTo(secondDiary.getEnd());
            softly.assertThat(diaryResponse.isLiked()).isTrue();
            softly.assertThat(diaryResponse.likeCount()).isEqualTo(1);
            softly.assertThat(content.getLast().likeCount()).isEqualTo(0);
            softly.assertThat(content.getLast().isLiked()).isFalse();
            softly.assertThat(content.getLast().isStored()).isFalse();
            softly.assertThat(content.getFirst().diaryId() > content.getLast().diaryId()).isTrue();
        });
    }

    @Test
    @DisplayName("타인 다이어리 조회 및 scope, likeCount 확인")
    void getUserDiariesByViewer() throws Exception {
        DiaryEntity publicDiary = diaryLowService.saveDiary(TestUtil.makeDiaryEntityWithScope(testUser, DiaryScope.PUBLIC));
        DiaryEntity killingPartDiary = diaryLowService.saveDiary(TestUtil.makeDiaryEntityWithScope(testUser, DiaryScope.KILLING_PART));
        DiaryEntity privateDiary = diaryLowService.saveDiary(TestUtil.makeDiaryEntityWithScope(testUser, DiaryScope.PRIVATE));

        UserEntity viewer = userRepository.save(TestUtil.makeDifferentUserEntity(testUser));

        diaryLikeLowService.saveDiaryLike(new DiaryLikeEntity(viewer, publicDiary));
        diaryLikeLowService.saveDiaryLike(new DiaryLikeEntity(testUser, publicDiary));
        diaryStoreRepository.save(new DiaryStoreEntity(viewer, publicDiary));

        diaryLikeLowService.saveDiaryLike(new DiaryLikeEntity(testUser, killingPartDiary));

        TestSecurityContextHolderInjection.inject(viewer.getId(), viewer.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/diaries/user/{userId}", testUser.getId()) // testUser의 다이어리 조회
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        // then
        JsonNode jsonNode = objectMapper.readTree(response);
        List<UserDiaryResponseDto> content = objectMapper.convertValue(
                jsonNode.path("content"), new TypeReference<>() {}
        );

        Map<Long, UserDiaryResponseDto> responseMap = content.stream()
                .collect(Collectors.toMap(UserDiaryResponseDto::diaryId, Function.identity()));

        assertSoftly(softly -> {
            softly.assertThat(content).hasSize(2);
            softly.assertThat(responseMap.containsKey(privateDiary.getId())).isFalse();
            softly.assertThat(content.getFirst().diaryId() > content.getLast().diaryId()).isTrue();

            // PUBLIC
            UserDiaryResponseDto publicResponse = responseMap.get(publicDiary.getId());
            softly.assertThat(publicResponse.content()).isNotNull();
            softly.assertThat(publicResponse.content()).isEqualTo(publicDiary.getContent());
            softly.assertThat(publicResponse.isLiked()).isTrue();
            softly.assertThat(publicResponse.isStored()).isTrue();
            softly.assertThat(publicResponse.likeCount()).isEqualTo(2L);

            // KILLING_PART
            UserDiaryResponseDto killingPartResponse = responseMap.get(killingPartDiary.getId());
            softly.assertThat(killingPartResponse.content()).isEqualTo("비공개 일기입니다.");
            softly.assertThat(killingPartResponse.isLiked()).isFalse();
            softly.assertThat(killingPartResponse.isStored()).isFalse();
            softly.assertThat(killingPartResponse.likeCount()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("내 다이어리 기간 조회 (캘린더 용)")
    void getMyDiariesByPeriod() throws Exception {
        // given
        diaryRepository.save(TestUtil.makeDiaryEntity(testUser));
        diaryRepository.save(TestUtil.makeDiaryEntity(testUser));

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/diaries/my/calendar")
                        .param("start", startDate.toString())
                        .param("end", endDate.toString())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        List<CalendarDiaryResponseDto> content = objectMapper.readValue(response, new TypeReference<>() {});

        assertSoftly(softly -> {
            softly.assertThat(content.getFirst().diaryId() > content.getLast().diaryId()).isTrue();
            softly.assertThat(content).hasSize(2);
        });
    }

    @Test
    @DisplayName("내 피드 조회")
    void getMyDiariesFeeds() throws Exception {
        // given

        //구독 한 회원
        UserEntity subscribedToUser = userRepository.save(TestUtil.makeDifferentUserEntity(testUser));
        Subscribe subscribe = new Subscribe(testUser, subscribedToUser);
        subscribeRepository.save(subscribe);

        DiaryEntity otherFeedDiary = diaryRepository.save(TestUtil.makeDiaryEntity(subscribedToUser));
        // 좋아요, 저장 누를 다이어를 두 번째로 저장
        DiaryEntity likedFeedDiary = diaryRepository.save(TestUtil.makeDiaryEntity(subscribedToUser));
        // 좋아요 누르기
        DiaryLikeEntity diaryLikeEntity = diaryLikeLowService.saveDiaryLike(new DiaryLikeEntity(testUser, likedFeedDiary));
        DiaryStoreEntity diaryStoreEntity = diaryStoreRepository.save(new DiaryStoreEntity(testUser, likedFeedDiary));


        // 구독하지 않은 회원
        UserEntity otherUser = userRepository.save(TestUtil.makeDifferentUserEntity(subscribedToUser));
        DiaryEntity noneFeedDiary = diaryRepository.save(TestUtil.makeDiaryEntity(otherUser));

        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/diaries/my/feeds")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        // then
        JsonNode jsonNode = objectMapper.readTree(response);
        List<FeedDiaryResponseDto> content = objectMapper.convertValue(
                jsonNode.path("content"), new TypeReference<>() {}
        );

        FeedDiaryResponseDto feedDiaryResponseDto = content.getFirst();

        assertSoftly(softly -> {
            softly.assertThat(content).hasSize(2);
            softly.assertThat(feedDiaryResponseDto.diaryId()).isEqualTo(likedFeedDiary.getId());
            softly.assertThat(feedDiaryResponseDto.isLiked()).isTrue();
            softly.assertThat(feedDiaryResponseDto.isStored()).isTrue();
            softly.assertThat(feedDiaryResponseDto.likeCount()).isEqualTo(1L);
            softly.assertThat(feedDiaryResponseDto.userId()).isEqualTo(subscribedToUser.getId());
            softly.assertThat(content.getFirst().diaryId() > content.getLast().diaryId()).isTrue();
        });
    }

    @Test
    @DisplayName("다이어리 생성 API")
    void createDiary() throws Exception {
        // given
        DiaryCreateRequest diaryRequest = TestUtil.makeDiaryCreateRequest();

        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        // when & then
        String header = mockMvc.perform(post("/api/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diaryRequest))
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(MockMvcResultMatchers.header().exists("location"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("location");

        Long id = Long.parseLong(header.substring(header.lastIndexOf("/") + 1));

        DiaryEntity diaryEntity = diaryRepository.findById(id).get();

        assertSoftly(softly-> {
            softly.assertThat(diaryEntity.getAlbumImageUrl()).isEqualTo(diaryRequest.albumImageUrl());
            softly.assertThat(diaryEntity.getEnd()).isEqualTo(diaryRequest.end());
            softly.assertThat(diaryEntity.getDuration()).isEqualTo(diaryRequest.duration());
            softly.assertThat(diaryEntity.getTotalDuration()).isEqualTo(diaryRequest.totalDuration());
            softly.assertThat(diaryEntity.getStart()).isEqualTo(diaryRequest.start());
            softly.assertThat(diaryEntity.getArtist()).isEqualTo(diaryRequest.artist());
            softly.assertThat(diaryEntity.getMusicTitle()).isEqualTo(diaryRequest.musicTitle());
            softly.assertThat(diaryEntity.getScope()).isEqualTo(diaryRequest.scope());
        });
    }

    @Test
    @DisplayName("다이어리 수정 API")
    void updateDiary() throws Exception {
        // given
        DiaryEntity diary = diaryRepository.save(TestUtil.makeDiaryEntity(testUser));
        DiaryUpdateRequestDto updateRequest = TestUtil.makeDiaryUpdateRequest();
        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        // when
        mockMvc.perform(patch("/api/diaries/{diaryId}", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        DiaryEntity updatedDiary = diaryRepository.findById(diary.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(updatedDiary.getMusicTitle()).isEqualTo(updateRequest.musicTitle());
            softly.assertThat(updatedDiary.getTotalDuration()).isEqualTo(updateRequest.totalDuration());
            softly.assertThat(updatedDiary.getContent()).isEqualTo(updateRequest.content());
        });
    }

    @Test
    @DisplayName("다이어리 삭제 API")
    void deleteDiary() throws Exception {
        // given
        DiaryEntity diary = diaryRepository.save(TestUtil.makeDiaryEntity(testUser));
        diaryStoreRepository.save(new DiaryStoreEntity(testUser, diary));
        diaryReportRepository.save(new DiaryReportEntity("나쁜말해요", diary.getContent(), diary, testUser));
        DiaryLikeEntity diaryLikeEntity = diaryLikeLowService.saveDiaryLike(new DiaryLikeEntity(testUser, diary));
        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        // when & then
        mockMvc.perform(delete("/api/diaries/{diaryId}", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isNoContent());


        assertSoftly(softly -> {
            softly.assertThat(diaryRepository.existsById(diary.getId())).isFalse();
            softly.assertThat(diaryLikeLowService.existsByUserAndDiary(testUser, diary)).isFalse();
        });
    }

    @Test
    @DisplayName("랜덤 다이어리 조회")
    void getRandomDiaries() throws Exception {

        // given
        DiaryEntity diary = diaryRepository.save(TestUtil.makeDiaryEntity(testUser));

        TestSecurityContextHolderInjection.inject(testUser.getId(), testUser.getRoleType());

        // when & then
        String response = mockMvc.perform(get("/api/diaries/randoms")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<FeedDiaryResponseDto> feedDiaryResponseDtos = objectMapper.readValue(response, new TypeReference<List<FeedDiaryResponseDto>>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(feedDiaryResponseDtos.size()).isEqualTo(1);
            softly.assertThat(feedDiaryResponseDtos.get(0).diaryId()).isEqualTo(diary.getId());
        });
    }
}
