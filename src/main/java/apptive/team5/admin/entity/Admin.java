package apptive.team5.admin.entity;

import apptive.team5.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_user")
@Getter
@NoArgsConstructor
public class Admin extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String adminId;

    @Column(nullable = false)
    private String password;

    public Admin(String adminId, String password) {
        this.adminId = adminId;
        this.password = password;
    }
}
