package apptive.team5.user.controller;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryLikeEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.repository.DiaryLikeRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.diary.repository.DiaryStoreRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.util.S3Util;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.repository.SubscribeRepository;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.dto.UserSearchResponse;
import apptive.team5.user.dto.UserStaticsResponse;
import apptive.team5.user.dto.UserTagUpdateRequest;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private DiaryLikeRepository diaryLikeRepository;

    @Autowired
    private DiaryStoreRepository  diaryStoreRepository;


    @DisplayName("회원 정보 조회 성공")
    @Test
    void getMyInfoSuccess() throws Exception {

        // given
        UserEntity user = TestUtil.makeUserEntity();
        userRepository.save(user);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/users/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString();

        // then
        UserResponse userResponse = objectMapper.readValue(response, UserResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(userResponse.identifier()).isEqualTo(user.getIdentifier());
            softly.assertThat(userResponse.socialType()).isEqualTo(user.getSocialType());
            softly.assertThat(userResponse.userRoleType()).isEqualTo(user.getRoleType());
            softly.assertThat(S3Util.extractFileName(userResponse.profileImageUrl())).isEqualTo(user.getProfileImage());
        });
    }

    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    @Test
    void getMyInfoFail() throws Exception {

        // given
        TestSecurityContextHolderInjection.inject(1L, UserRoleType.USER);


        // when
        MockHttpServletResponse response = mockMvc.perform(get("/api/users/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse();

        String content = response.getContentAsString();


        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            softly.assertThat(content.contains(ExceptionCode.NOT_FOUND_USER.getDescription()));
        });

    }

    @DisplayName("회원 tag 변경 성공")
    @Test
    void changeUserTagSuccess() throws Exception {

        // given
        UserEntity user = TestUtil.makeUserEntity();
        userRepository.save(user);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        UserTagUpdateRequest userTagUpdateRequest = new UserTagUpdateRequest("abc_dd");


        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/users/my/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userTagUpdateRequest))
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        String content = response.getContentAsString();

        UserResponse userResponse = objectMapper.readValue(content, UserResponse.class);


        assertSoftly(softly -> {
            softly.assertThat(userResponse.userId()).isEqualTo(user.getId());
            softly.assertThat(userResponse.tag()).isEqualTo(userTagUpdateRequest.tag());
        });

    }

    @DisplayName("회원 tag 변경 실패 - 중복된 태그")
    @Test
    void changeUserTagFail() throws Exception {

        // given

        UserEntity user = TestUtil.makeUserEntity();
        UserEntity user2 = TestUtil.makeDifferentUserEntity(user);
        userRepository.save(user);
        userRepository.save(user2);
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
        UserTagUpdateRequest userTagUpdateRequest = new UserTagUpdateRequest(user2.getTag());


        // when
        MockHttpServletResponse response = mockMvc.perform(patch("/api/users/my/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userTagUpdateRequest))
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse();

        // then
        String content = response.getContentAsString();

        Map<String,String> apiResponse = objectMapper.readValue(content, Map.class);

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.get("message")).isEqualTo(ExceptionCode.DUPLICATE_USER_TAG.getDescription());
        });

    }

    @DisplayName("tag를 통해 유저 조회 성공")
    @Test
    void getUserByTagSuccess() throws Exception {

        // given
        UserEntity user = TestUtil.makeUserEntity();
        userRepository.save(user);
        UserEntity subscribedToUser = TestUtil.makeDifferentUserEntity(user);
        userRepository.save(subscribedToUser);
        subscribeRepository.save(new Subscribe(user, subscribedToUser));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/users")
                        .param("searchCond", user.getTag())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(UTF_8);

        // then
        JsonNode jsonNode = objectMapper.readTree(response);

        List<UserSearchResponse> content = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<UserSearchResponse>>() {}
        );


        assertSoftly(softly -> {
            softly.assertThat(content.size()).isEqualTo(2);
            softly.assertThat(content.get(0).userId()).isEqualTo(user.getId());
            softly.assertThat(content.get(0).isMyPick()).isFalse();
            softly.assertThat(content.get(1).userId()).isEqualTo(subscribedToUser.getId());
            softly.assertThat(content.get(1).isMyPick()).isTrue();
        });

    }

    @DisplayName("username 통해 유저 조회 성공")
    @Test
    void getUserByTUsernameSuccess() throws Exception {

        // given
        UserEntity user = TestUtil.makeUserEntity();
        userRepository.save(user);
        UserEntity subscribedToUser = TestUtil.makeDifferentUserEntity(user);
        userRepository.save(subscribedToUser);
        subscribeRepository.save(new Subscribe(user, subscribedToUser));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/users")
                        .param("searchCond", user.getUsername())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(UTF_8);

        // then
        JsonNode jsonNode = objectMapper.readTree(response);

        List<UserSearchResponse> content = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<UserSearchResponse>>() {}
        );


        assertSoftly(softly -> {
            softly.assertThat(content.size()).isEqualTo(2);
            softly.assertThat(content.get(0).userId()).isEqualTo(user.getId());
            softly.assertThat(content.get(0).isMyPick()).isFalse();
            softly.assertThat(content.get(1).userId()).isEqualTo(subscribedToUser.getId());
            softly.assertThat(content.get(1).isMyPick()).isTrue();
        });

    }

    @DisplayName("회원 탈퇴 성공")
    @Test
    void withDrawSuccess() throws Exception {

        // given
        UserEntity user = TestUtil.makeUserEntity();
        UserEntity otherUser = TestUtil.makeDifferentUserEntity(user);
        userRepository.save(user);
        userRepository.save(otherUser);
        DiaryEntity diaryEntity = TestUtil.makeDiaryEntity(user);
        DiaryEntity otherDiary = TestUtil.makeDiaryEntity(otherUser);
        diaryRepository.save(diaryEntity);
        diaryRepository.save(otherDiary);
        DiaryLikeEntity diaryLikeEntity = new DiaryLikeEntity(user, diaryEntity);
        DiaryLikeEntity otherDiaryLikeEntity = new DiaryLikeEntity(user, otherDiary);
        diaryLikeRepository.save(diaryLikeEntity);
        diaryLikeRepository.save(otherDiaryLikeEntity);
        diaryStoreRepository.save(new DiaryStoreEntity(user, diaryEntity));
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());


        // when
        mockMvc.perform(delete("/api/users/my")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isNoContent());

        // then
        assertSoftly(softly -> {
            softly.assertThat(userRepository.existsById(user.getId())).isFalse();
            softly.assertThat(diaryRepository.existsById(diaryEntity.getId())).isFalse();
            softly.assertThat(diaryLikeRepository.existsById(diaryLikeEntity.getId())).isFalse();
            softly.assertThat(diaryLikeRepository.existsById(otherDiaryLikeEntity.getId())).isFalse();
            softly.assertThat(diaryRepository.existsById(otherDiary.getId())).isTrue();
        });

    }

    @DisplayName("회원 통계 조회")
    @Test
    void getUserStaticsSuccess() throws Exception {

        // given
        UserEntity subscriber = TestUtil.makeUserEntity();
        userRepository.save(subscriber);
        UserEntity subscribedTo = TestUtil.makeDifferentUserEntity(subscriber);
        userRepository.save(subscribedTo);
        Subscribe subscribe = subscribeRepository.save(new Subscribe(subscriber, subscribedTo));
        DiaryEntity diaryEntity = diaryRepository.save(TestUtil.makeDiaryEntity(subscriber));

        TestSecurityContextHolderInjection.inject(subscriber.getId(), subscriber.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/users/{userId}/statics", subscriber.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserStaticsResponse userStaticsResponse = objectMapper.readValue(response, UserStaticsResponse.class);


        assertSoftly(softly -> {
            softly.assertThat(userStaticsResponse.fanCount()).isEqualTo(0);
            softly.assertThat(userStaticsResponse.pickCount()).isEqualTo(1);
            softly.assertThat(userStaticsResponse.killingPartCount()).isEqualTo(1);
        });
    }


}
