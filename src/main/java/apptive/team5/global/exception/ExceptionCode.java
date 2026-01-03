package apptive.team5.global.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {

    NOT_FOUND_USER("존재하지 않는 회원입니다."),
    NOT_EXIST_REFRESH_TOKEN("리프래시 토큰이 없습니다."),
    INVALID_REFRESH_TOKEN("잘못된 리프래시 토큰입니다."),
    YOUTUBE_API_EXCEPTION("유튜브 api 호출 실패"),
    KAKAO_API_EXCEPTION("카카오 api 호출 실패"),
    GOOGLE_API_EXCEPTION("구글 api 호출 실패"),
    DUPLICATE_USER_TAG("이미 존재하는 tag입니다."),
    DUPLICATE_DIARY_LIKE("이미 좋아요를 누르셨습니다!"),
    ACCESS_DENIED_DIARY("해당 다이어리에 대한 권한이 없습니다."),
    NOT_FOUND_DIARY("그런 다이어리는 없습니다."),
    NOT_FOUND_DIARY_LIKE("좋아요를 누르지 않은 킬링파트입니다!"),
    BAD_SUBSCRIBE_REQUEST("자기 자신은 구독할 수 없습니다."),
    DUPLICATE_SUBSCRIBE_REQUEST("이미 구독한 회원입니다.");

    private final String description;


    ExceptionCode(String description) {
        this.description = description;
    }
}
