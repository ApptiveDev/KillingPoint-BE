# 공지 API 명세

공지 API는 로그인한 앱 사용자가 공지 목록과 본문을 조회할 때 사용합니다. 관리자 화면에서 공지를 등록하면 공지 Entity에 저장하고, 전체 사용자에게 알림 내역을 생성합니다. 푸시 알림은 사용자 알림 설정을 따릅니다.

## 인증

요청에 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

## 공지 목록 조회

`GET /api/notices?page=0&size=20`

| 파라미터 | 타입 | 기본값 | 제약 |
| --- | --- | --- | --- |
| `page` | integer | `0` | 0 이상 |
| `size` | integer | `20` | 1~50 |

최신 공지 순으로 조회합니다. 본문은 목록에서 제외하고 단건 조회에서 가져옵니다.

### 200 응답 예시

```json
{
  "content": [
    {
      "noticeId": 12,
      "title": "서비스 점검 안내",
      "createdAt": "2026-10-25T22:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "numberOfElements": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

## 공지 상세 조회

`GET /api/notices/{noticeId}`

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `noticeId` | long | 공지 ID (1 이상) |

### 200 응답 예시

```json
{
  "noticeId": 12,
  "title": "서비스 점검 안내",
  "content": "점검 시간은 2026년 10월 26일 02:00~03:00입니다.",
  "createdAt": "2026-10-25T22:00:00"
}
```

### 오류 응답

| HTTP 상태 | 상황 | 응답 예시 |
| --- | --- | --- |
| `400 Bad Request` | 페이지/크기/ID 범위 오류 | `{ "message": "..." }` |
| `401 Unauthorized` | 토큰 누락 또는 만료 | `{ "message": "..." }` |
| `404 Not Found` | 존재하지 않는 공지 ID | `{ "message": "공지를 찾을 수 없습니다." }` |

## 공지 등록 후 앱 알림

관리자 화면에서 공지를 등록하면 다음 알림이 생성됩니다.

- 알림 제목: `새 공지가 등록됐어요`
- 알림 본문: 공지 제목
- 딥링크: `/api/notices/{noticeId}`
- 알림 내역은 전체 사용자에게 생성하고, FCM 푸시는 사용자 알림 설정이 켜진 경우 전송합니다.
