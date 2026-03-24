package apptive.team5.user.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "policy_type"}))
public class UserPolicyAgreementEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false)
    private PolicyType policyType;

    @Column(nullable = false)
    private Boolean agreed;

    @Column(nullable = false)
    private Long revision;

    private LocalDateTime agreedAt;

    public UserPolicyAgreementEntity(UserEntity userEntity, PolicyType policyType, boolean agreed, Long revision) {
        this.userEntity = userEntity;
        this.policyType = policyType;
        this.agreed = agreed;
        this.revision = revision;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }

    public void updateAgreement(boolean agreed, Long revision) {
        this.agreed = agreed;
        this.revision = revision;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }

    public boolean needsUpdate(Long latestRevision) {
        return !agreed || revision < latestRevision;
    }
}
