package apptive.team5.diary.domain;

import apptive.team5.global.converter.LongListConverter;
import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_order_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Convert(converter = LongListConverter.class)
    @Column(name = "diary_orders", columnDefinition = "TEXT")
    private List<Long> orderList;

    public DiaryOrderEntity(UserEntity user, List<Long> orderList) {
        this.user = user;
        this.orderList = orderList;
    }

    public void updateOrder(List<Long> orderList) {
        this.orderList = orderList;
    }

    public void addDiaryId(Long diaryId) {
        if (this.orderList == null) {
            this.orderList = new ArrayList<>();
        }
        else {
            this.orderList = new ArrayList<>(this.orderList);
        }
        this.orderList.addFirst(diaryId);
    }

    public void removeDiaryId(Long diaryId) {
        if (this.orderList == null) {
            return;
        }
        this.orderList = new ArrayList<>(this.orderList);
        this.orderList.remove(diaryId);
    }
}
