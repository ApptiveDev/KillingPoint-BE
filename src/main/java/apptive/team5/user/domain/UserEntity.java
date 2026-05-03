package apptive.team5.user.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.jwt.domain.RefreshToken;
import apptive.team5.oauth2.dto.OAuth2Response;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    private static String DEFAULT_IMAGE = "defaultImage/userDefaultImage.png";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String identifier;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoleType roleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(nullable = false)
    private String profileImage;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean alarmEnabled = true;

    public UserEntity(String identifier, String email, String username, String tag, UserRoleType roleType, SocialType socialType) {
        this.identifier = identifier;
        this.email = email;
        this.username = username;
        this.tag = tag;
        this.roleType = roleType;
        this.socialType = socialType;
        this.profileImage = DEFAULT_IMAGE;
        this.alarmEnabled = true;
    }

    public UserEntity(Long id, String identifier, String email, String username, String tag, UserRoleType roleType, SocialType socialType) {
        this.id = id;
        this.identifier = identifier;
        this.email = email;
        this.username = username;
        this.tag = tag;
        this.roleType = roleType;
        this.socialType = socialType;
        this.profileImage = DEFAULT_IMAGE;
        this.alarmEnabled = true;
    }

    public void changeTag(String tag) {
        this.tag = tag;
    }

    public void changeName(String username) {
        this.username = username;
    }

    public void changeProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setDefaultImage() {
        this.profileImage = DEFAULT_IMAGE;
    }

    public void changeAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }
}
