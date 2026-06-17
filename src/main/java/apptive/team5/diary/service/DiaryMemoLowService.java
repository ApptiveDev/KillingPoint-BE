package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryMemoEntity;
import apptive.team5.diary.repository.DiaryMemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryMemoLowService {

    private final DiaryMemoRepository diaryMemoRepository;

    public DiaryMemoEntity save(DiaryMemoEntity diaryMemo) {
        return diaryMemoRepository.save(diaryMemo);
    }

    public void deleteById(Long memoId) {
        diaryMemoRepository.deleteById(memoId);
    }

    @Transactional(readOnly = true)
    public long count() {
        return diaryMemoRepository.count();
    }

    public void deleteByDiaryId(Long diaryId) {
        diaryMemoRepository.deleteByDiaryId(diaryId);
    }

    public void deleteByDiaryIds(List<Long> diaryIds) {
        if (diaryIds.isEmpty()) {
            return;
        }

        diaryMemoRepository.deleteByDiaryIds(diaryIds);
    }
}
