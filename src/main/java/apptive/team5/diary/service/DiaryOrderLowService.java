package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryOrderEntity;
import apptive.team5.diary.repository.DiaryOrderRepository;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryOrderLowService {
    private final DiaryOrderRepository diaryOrderRepository;

    @Transactional(readOnly = true)
    public Optional<DiaryOrderEntity> findByUserId(Long userId) {
        return diaryOrderRepository.findByUserId(userId);
    }

    public void saveOrder(UserEntity user, List<Long> diaryIds) {
        Optional<DiaryOrderEntity> optionalDiaryOrder = diaryOrderRepository.findByUserId(user.getId());

        if (optionalDiaryOrder.isPresent()) {
            optionalDiaryOrder.get().updateOrder(diaryIds);
        }
        else {
            diaryOrderRepository.save(new DiaryOrderEntity(user, diaryIds));
        }
    }

    public void addDiaryId(Long userId, Long diaryId) {
        diaryOrderRepository.findByUserId(userId)
                .ifPresent(order -> order.addDiaryId(diaryId));
    }

    public void deleteDiaryId(Long userId, Long diaryId) {
        diaryOrderRepository.findByUserId(userId)
                .ifPresent(order -> order.removeDiaryId(diaryId));
    }

    public void deleteByUserId(Long userId) {
        diaryOrderRepository.deleteByUserId(userId);
    }
}
