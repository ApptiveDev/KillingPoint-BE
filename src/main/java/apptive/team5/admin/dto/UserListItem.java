package apptive.team5.admin.dto;

import apptive.team5.user.domain.UserEntity;

public record UserListItem(
        Long userId,
        String email,
        String username,
        String tag,
        String socialType,
        String roleType,
        boolean locked,
        String lockedLabel,
        String lockedStatusClass
) {

    public static UserListItem from(UserEntity user) {
        return new UserListItem(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getTag(),
                user.getSocialType().name(),
                user.getRoleType().name(),
                user.isLocked(),
                user.isLocked() ? "정지" : "정상",
                user.isLocked() ? "reported" : ""
        );
    }
}
