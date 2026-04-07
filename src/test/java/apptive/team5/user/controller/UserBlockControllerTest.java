package apptive.team5.user.controller;

import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.repository.SubscribeRepository;
import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.repository.UserBlockRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserBlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("회원 차단 성공")
    @Test
    void addBlockedUserSuccess() throws Exception {

        // given
        UserEntity blocker = userRepository.save(TestUtil.makeUserEntity());
        UserEntity blockedUser = userRepository.save(TestUtil.makeDifferentUserEntity(blocker));

        subscribeRepository.save(new Subscribe(blocker, blockedUser));
        subscribeRepository.save(new Subscribe(blockedUser, blocker));

        TestSecurityContextHolderInjection.inject(blocker.getId(), blocker.getRoleType());

        // when
        mockMvc.perform(post("/api/users/{blockedId}/blocks", blockedUser.getId())
                .with(securityContext(SecurityContextHolder.getContext()))
        ).andExpect(status().isCreated());

        // then
        List<UserBlock> userBlocks = userBlockRepository.findAll();

        assertSoftly(softly -> {
            softly.assertThat(userBlocks).hasSize(1);
            softly.assertThat(userBlocks.getFirst().getBlocker().getId()).isEqualTo(blocker.getId());
            softly.assertThat(userBlocks.getFirst().getBlockedUser().getId()).isEqualTo(blockedUser.getId());
            softly.assertThat(subscribeRepository.existsBySubscriberIdAndSubscribedToId(blockedUser.getId(), blocker.getId())).isFalse();
            softly.assertThat(subscribeRepository.existsBySubscriberIdAndSubscribedToId(blocker.getId(), blockedUser.getId())).isFalse();
        });
    }

    @DisplayName("회원 차단 실패 - 존재하지 않는 회원")
    @Test
    void addBlockedUserFailCase1() throws Exception {

        // given
        UserEntity blocker = userRepository.save(TestUtil.makeUserEntity());

        TestSecurityContextHolderInjection.inject(blocker.getId(), blocker.getRoleType());

        // when & then
        mockMvc.perform(post("/api/users/{blockedId}/blocks", 100L)
                .with(securityContext(SecurityContextHolder.getContext()))
        ).andExpect(status().isNotFound());
    }

    @DisplayName("회원 차단 실패 - 이미 차단한 회원")
    @Test
    void addBlockedUserFailCase2() throws Exception {

        // given
        UserEntity blocker = userRepository.save(TestUtil.makeUserEntity());
        UserEntity blockedUser = userRepository.save(TestUtil.makeDifferentUserEntity(blocker));
        userBlockRepository.save(new UserBlock(blocker, blockedUser));

        TestSecurityContextHolderInjection.inject(blocker.getId(), blocker.getRoleType());

        // when
        String response = mockMvc.perform(post("/api/users/{blockedId}/blocks", blockedUser.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                ).andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(UTF_8);

        // then
        Map<String, String> apiResponse = objectMapper.readValue(response, new TypeReference<Map<String, String>>() {
        });

        assertThat(apiResponse.get("message")).isEqualTo(ExceptionCode.DUPLICATE_BLOCKED_USER.getDescription());
    }

    @DisplayName("차단한 회원 목록 조회 성공")
    @Test
    void getBlockedUserSuccess() throws Exception {

        // given
        UserEntity blocker = userRepository.save(TestUtil.makeUserEntity());
        UserEntity blockedUser1 = userRepository.save(TestUtil.makeDifferentUserEntity(blocker));
        UserEntity blockedUser2 = userRepository.save(TestUtil.makeDifferentUserEntity(blockedUser1));
        userBlockRepository.save(new UserBlock(blocker, blockedUser1));
        userBlockRepository.save(new UserBlock(blocker, blockedUser2));

        TestSecurityContextHolderInjection.inject(blocker.getId(), blocker.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/users/blocks")
                        .with(securityContext(SecurityContextHolder.getContext()))
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(UTF_8);

        // then
        JsonNode jsonNode = objectMapper.readTree(response);
        List<UserResponse> content = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<UserResponse>>() {
                }
        );

        assertSoftly(softly -> {
            softly.assertThat(content).hasSize(2);
            softly.assertThat(content).extracting(UserResponse::userId)
                    .containsExactlyInAnyOrder(blockedUser1.getId(), blockedUser2.getId());
        });
    }
}
