package apptive.team5.diary.controller;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryOrderEntity;
import apptive.team5.diary.dto.DiaryOrderUpdateRequestDto;
import apptive.team5.diary.repository.DiaryOrderRepository;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DiaryOrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private DiaryOrderRepository diaryOrderRepository;

    private UserEntity user;
    private DiaryEntity diary1;
    private DiaryEntity diary2;
    private DiaryEntity diary3;

    @BeforeEach
    void setUp() {
        user = TestUtil.makeUserEntity();

        userRepository.save(user);

        diary1 = TestUtil.makeDiaryEntity(user);
        diary2 = TestUtil.makeDiaryEntity(user);
        diary3 = TestUtil.makeDiaryEntity(user);

        diaryRepository.saveAll(List.of(diary1, diary2, diary3));
    }

    @Test
    @DisplayName("순서 변경 후 조회")
    void updateDiaryOrder() throws Exception {
        // given
        List<Long> newOrder = List.of(diary3.getId(), diary1.getId(), diary2.getId());
        DiaryOrderUpdateRequestDto requestDto = new DiaryOrderUpdateRequestDto(newOrder);

        // when
        mockMvc.perform(patch("/api/diaries/order")
                .with(authentication(createAuthentication(user)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        // then
        DiaryOrderEntity diaryOrder = diaryOrderRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AssertionError("정렬 순서 엔티티 없음"));

        assertThat(diaryOrder.getOrderList()).hasSize(3)
                .containsExactly(diary3.getId(), diary1.getId(), diary2.getId());
    }

    @Test
    @DisplayName("빈 리스트 업데이트 시 순서 데이터 초기화")
    void updateDiaryOrderEmptyList() throws Exception {
        // given
        List<Long> existingOrder = List.of(diary1.getId(), diary2.getId());
        diaryOrderRepository.save(new DiaryOrderEntity(user, existingOrder));

        DiaryOrderUpdateRequestDto requestDto = new DiaryOrderUpdateRequestDto(List.of());

        // when
        mockMvc.perform(patch("/api/diaries/order")
                .with(authentication(createAuthentication(user)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        // then
        boolean exists = diaryOrderRepository.findByUserId(user.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    private UsernamePasswordAuthenticationToken createAuthentication(UserEntity user) {
        return new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRoleType().name()))
        );
    }
}
