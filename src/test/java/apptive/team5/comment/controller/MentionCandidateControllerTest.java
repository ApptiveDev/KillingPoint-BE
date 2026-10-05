package apptive.team5.comment.controller;

import apptive.team5.comment.dto.MentionCandidateResponse;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.repository.SubscribeRepository;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.repository.UserBlockRepository;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MentionCandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscribeRepository subscribeRepository;
    @Autowired
    private UserBlockRepository userBlockRepository;

    private UserEntity me;
    private UserEntity pickedKim;
    private UserEntity pickedLee;
    private UserEntity pickedBlocked;
    private UserEntity stranger;

    @BeforeEach
    void setUp() {
        me = userRepository.save(TestUtil.makeUserEntity());
        pickedKim = userRepository.save(new UserEntity("m-kim", "kim@test.local", "김기영", "kimgy", UserRoleType.USER, SocialType.KAKAO));
        pickedLee = userRepository.save(new UserEntity("m-lee", "lee@test.local", "이진원", "goodmusic", UserRoleType.USER, SocialType.KAKAO));
        pickedBlocked = userRepository.save(new UserEntity("m-blk", "blk@test.local", "김차단", "blocked1", UserRoleType.USER, SocialType.KAKAO));
        stranger = userRepository.save(new UserEntity("m-str", "str@test.local", "김낯선", "stranger1", UserRoleType.USER, SocialType.KAKAO));

        subscribeRepository.save(new Subscribe(me, pickedKim));
        subscribeRepository.save(new Subscribe(me, pickedLee));
        subscribeRepository.save(new Subscribe(me, pickedBlocked));
        subscribeRepository.save(new Subscribe(stranger, me));
        userBlockRepository.save(new UserBlock(me, pickedBlocked));

        TestSecurityContextHolderInjection.inject(me.getId(), me.getRoleType());
    }

    @Test
    @DisplayName("키워드 없이 호출하면 내가 픽한 사람만 이름순으로 나온다 (차단 제외)")
    void candidatesWithoutKeyword() throws Exception {
        List<MentionCandidateResponse> result = candidates(null, 10);

        assertThat(result).extracting(MentionCandidateResponse::userId)
                .containsExactly(pickedKim.getId(), pickedLee.getId());
    }

    @Test
    @DisplayName("키워드는 이름과 태그 양쪽에 contains 로 매칭된다")
    void candidatesWithKeyword() throws Exception {
        List<MentionCandidateResponse> byName = candidates("기영", 10);
        List<MentionCandidateResponse> byTag = candidates("goodm", 10);
        List<MentionCandidateResponse> none = candidates("낯선", 10);

        assertThat(byName).extracting(MentionCandidateResponse::userId).containsExactly(pickedKim.getId());
        assertThat(byTag).extracting(MentionCandidateResponse::userId).containsExactly(pickedLee.getId());
        assertThat(none).isEmpty();
    }

    @Test
    @DisplayName("나를 픽한 사람은 후보가 아니다")
    void fansAreNotCandidates() throws Exception {
        List<MentionCandidateResponse> result = candidates("김", 10);

        assertThat(result).extracting(MentionCandidateResponse::userId)
                .containsExactly(pickedKim.getId())
                .doesNotContain(stranger.getId(), pickedBlocked.getId());
    }

    @Test
    @DisplayName("size 만큼만 반환한다")
    void candidatesLimitedBySize() throws Exception {
        List<MentionCandidateResponse> result = candidates(null, 1);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().profileImageUrl()).startsWith("http");
    }

    private List<MentionCandidateResponse> candidates(String keyword, int size) throws Exception {
        TestSecurityContextHolderInjection.inject(me.getId(), me.getRoleType());
        var request = get("/api/comments/mention-candidates")
                .param("size", String.valueOf(size))
                .with(securityContext(SecurityContextHolder.getContext()));
        if (keyword != null) {
            request = request.param("keyword", keyword);
        }
        String body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(body, new TypeReference<List<MentionCandidateResponse>>() {});
    }
}
