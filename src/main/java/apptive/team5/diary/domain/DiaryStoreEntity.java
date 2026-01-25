package apptive.team5.diary.domain;

import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diary_store",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_diary_store_user_diary",
                        columnNames = {"user_id", "diary_id"}
                )
        }
)
public class DiaryStoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_diary_store_user_id_ref_user_id")
    )
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "diary_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_diary_store_diary_id_ref_diary_id")
    )
    private DiaryEntity diary;

    public DiaryStoreEntity(UserEntity user, DiaryEntity diary) {
        this.user = user;
        this.diary = diary;
    }
}
