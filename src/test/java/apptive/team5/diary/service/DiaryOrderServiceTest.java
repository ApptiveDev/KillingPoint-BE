package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.dto.DiaryOrderUpdateRequestDto;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static apptive.team5.global.exception.ExceptionCode.INVALID_DIARY_LIST;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DiaryOrderServiceTest {
    @InjectMocks
    private DiaryOrderService diaryOrderService;

    @Mock
    private DiaryOrderLowService diaryOrderLowService;
    @Mock
    private UserLowService userLowService;
    @Mock
    private DiaryLowService diaryLowService;

    @Test
    @DisplayName("정상적인 정렬 순서 변경 요청")
    void updateDiaryOrder() {
        // given
        Long userId = 1L;
        List<Long> requestedIds = List.of(10L, 20L);
        DiaryOrderUpdateRequestDto requestDto = new DiaryOrderUpdateRequestDto(requestedIds);

        UserEntity user = mock(UserEntity.class);
        given(userLowService.getReferenceById(userId)).willReturn(user);

        DiaryEntity d1 = mock(DiaryEntity.class);
        DiaryEntity d2 = mock(DiaryEntity.class);
        given(diaryLowService.findAllByIds(requestedIds)).willReturn(List.of(d1, d2));
        given(d1.isMyDiary(userId)).willReturn(true);
        given(d2.isMyDiary(userId)).willReturn(true);

        // when
        diaryOrderService.updateDiaryOrder(userId, requestDto);

        // then
        verify(diaryOrderLowService).saveOrder(user, requestedIds);
        verify(diaryOrderLowService, never()).deleteByUserId(any());
    }

    @Test
    @DisplayName("정렬 순서 초기화")
    void updateEmptyDiaryOrder() {
        // given
        Long userId = 1L;
        DiaryOrderUpdateRequestDto requestDto = new DiaryOrderUpdateRequestDto(List.of());

        // when
        diaryOrderService.updateDiaryOrder(userId, requestDto);

        // then
        verify(diaryOrderLowService).deleteByUserId(userId);
        verify(diaryOrderLowService, never()).saveOrder(any(), any());
        verify(diaryLowService, never()).findAllByIds(any());
    }

    @Test
    @DisplayName("존재하지 않는 다이어리 ID 포함 시 예외 처리")
    void updateInvalidDiaryOrder() {
        // given
        Long userId = 1L;
        List<Long> requestedIds = List.of(10L, 999L);
        DiaryOrderUpdateRequestDto requestDto = new DiaryOrderUpdateRequestDto(requestedIds);

        given(userLowService.getReferenceById(userId)).willReturn(mock(UserEntity.class));

        DiaryEntity d1 = mock(DiaryEntity.class);
        given(diaryLowService.findAllByIds(requestedIds)).willReturn(List.of(d1));
        given(d1.isMyDiary(userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> diaryOrderService.updateDiaryOrder(userId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_DIARY_LIST.getDescription());
    }

    @Test
    @DisplayName("다른 사람 다이어리 ID 포함 시 예외 처리")
    void updateOtherDiaryOrder() {
        // given
        Long userId = 1L;
        List<Long> requestedIds = List.of(10L, 20L);
        DiaryOrderUpdateRequestDto requestDto = new DiaryOrderUpdateRequestDto(requestedIds);

        given(userLowService.getReferenceById(userId)).willReturn(mock(UserEntity.class));

        DiaryEntity d1 = mock(DiaryEntity.class);
        DiaryEntity d2 = mock(DiaryEntity.class);

        given(diaryLowService.findAllByIds(requestedIds)).willReturn(List.of(d1, d2));
        given(d1.isMyDiary(userId)).willReturn(true);
        given(d2.isMyDiary(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> diaryOrderService.updateDiaryOrder(userId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_DIARY_LIST.getDescription());
    }
}
