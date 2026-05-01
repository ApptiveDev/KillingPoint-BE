package apptive.team5.alarm.controller;

import apptive.team5.alarm.dto.AlarmResponse;
import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.repository.AlarmRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("내 알림 목록 조회 성공")
    @Test
    void getAlarmsSuccess() throws Exception {

        // given
        UserEntity user = userRepository.save(TestUtil.makeUserEntity());
        UserEntity otherUser = userRepository.save(TestUtil.makeDifferentUserEntity(user));

        alarmRepository.save(new Alarm("first title", "first content", "/diaries/1", user));
        Alarm secondAlarm = alarmRepository.save(new Alarm("second title", "second content", "/diaries/2", user));
        alarmRepository.save(new Alarm("other title", "other content", "/diaries/3", otherUser));

        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());

        // when
        String response = mockMvc.perform(get("/api/alarms")
                        .param("page", "0")
                        .param("size", "1")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(UTF_8);

        // then
        JsonNode jsonNode = objectMapper.readTree(response);
        List<AlarmResponse> content = objectMapper.convertValue(
                jsonNode.path("content"),
                new TypeReference<List<AlarmResponse>>() {}
        );

        assertSoftly(softly -> {
            softly.assertThat(content).hasSize(1);
            softly.assertThat(content.getFirst().alarmId()).isEqualTo(secondAlarm.getId());
            softly.assertThat(content.getFirst().title()).isEqualTo(secondAlarm.getTitle());
            softly.assertThat(content.getFirst().content()).isEqualTo(secondAlarm.getContent());
            softly.assertThat(content.getFirst().deepLink()).isEqualTo(secondAlarm.getDeepLink());
            softly.assertThat(jsonNode.path("page").path("totalElements").asInt()).isEqualTo(2);
            softly.assertThat(jsonNode.path("page").path("size").asInt()).isEqualTo(1);
            softly.assertThat(jsonNode.path("page").path("number").asInt()).isEqualTo(0);
        });
    }
}
