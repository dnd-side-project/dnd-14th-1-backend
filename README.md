# DND 14th 1조 Backend

## Code Formatting (Spotless)

```bash
# 포맷 검사
./gradlew spotlessCheck

# 자동 포맷 적용
./gradlew spotlessApply
```

## API Versioning (Spring Boot 4)

### 규칙
- 버전 형식: `MAJOR.MINOR.PATCH` (예: `0.0.1`, `1.0.0`)
- 모든 Controller에 `version` 속성 필수
- 기본 버전: `0.0.1` (헤더 없이 요청 시 자동 적용)

### 사용법

**Controller 작성**
```java
@RestController
@RequestMapping(version = "0.0.1", path = "/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { ... }
}
```

**API 호출**
```bash
# 헤더로 버전 지정
curl -H "X-API-Version: 0.0.1" http://localhost:8080/users/1

# 헤더 생략 시 기본 버전(0.0.1) 적용
curl http://localhost:8080/users/1
```

### 버전 업그레이드 예시
```java
// v0.0.1 - 기존 API
@GetMapping(version = "0.0.1", path = "/{id}")
public UserV1 getUserV1(@PathVariable Long id) { ... }

// v0.0.2 - 새 버전 API
@GetMapping(version = "0.0.2", path = "/{id}")
public UserV2 getUserV2(@PathVariable Long id) { ... }
```

## Common Response (공통 응답)

### 위치
- `com.rokyai.dnd14th1backend.common.response` 패키지
- `com.rokyai.dnd14th1backend.common.exception` 패키지

### 구성 요소

| 클래스/인터페이스 | 설명 |
|-----------------|------|
| `ApiResponse<T>` | 성공 응답 래퍼 (status, data, message) |
| `ApiExceptionResponse<T>` | 예외 응답 래퍼 (status, data, message, customStatusCode) |
| `StatusInterface` | 상태 코드 인터페이스 |
| `DefaultStatus` | 기본 상태 코드 Enum (0~99 범위) |
| `ApiException` | 비즈니스 예외 기본 클래스 |
| `ApiExceptionHandler` | 전역 예외 처리기 |
| `ApiResponseWrapper` | 응답 자동 래핑 (봉투 패턴) |
| `SkipApiResponseWrapper` | 래핑 제외 어노테이션 |

### 사용법

**Controller에서 원시 데이터 반환 (자동 래핑)**
```java
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
// 응답: { "status": 200, "data": {...}, "message": "성공" }
```

**예외 발생**
```java
// DefaultStatus 사용
throw new ApiException(DefaultStatus.NOT_FOUND);

// 커스텀 메시지
throw new ApiException(DefaultStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다");

// 도메인별 Status 사용
throw new ApiException(UserStatus.ALREADY_EXISTS);
```

**도메인별 Status 정의**
```java
@Getter
@RequiredArgsConstructor
public enum UserStatus implements StatusInterface {
    ALREADY_EXISTS(409, 10, "이미 존재하는 사용자입니다"),
    NOT_VERIFIED(403, 11, "이메일 인증이 필요합니다");

    private final int httpStatusCode;
    private final int customStatusCode;
    private final String description;
}
```

**래핑 제외 (파일 다운로드, SSE 등)**
```java
@GetMapping("/download")
@SkipApiResponseWrapper
public ResponseEntity<Resource> downloadFile() { ... }
```

### 응답 형식

**성공 응답**
```json
{
  "status": 200,
  "data": { "id": 1, "name": "홍길동" },
  "message": "성공"
}
```

**예외 응답**
```json
{
  "status": 400,
  "data": null,
  "message": "잘못된 요청입니다",
  "customStatusCode": 1
}
```

### CustomStatusCode 규칙

| 범위 | 용도 |
|-----|------|
| 0 | 성공 |
| 1~9 | 기본 클라이언트 에러 |
| 10~19 | User 도메인 |
| 20~29 | Order 도메인 |
| 90~99 | 서버 에러 |

### 기본 상태 코드 (DefaultStatus)

| 상태 | HTTP | customStatusCode | 설명 |
|-----|------|------------------|------|
| OK | 200 | 20               | 성공 |
| BAD_REQUEST | 400 | 400              | 잘못된 요청 |
| UNAUTHORIZED | 401 | 401              | 인증 필요 |
| FORBIDDEN | 403 | 403              | 접근 권한 없음 |
| NOT_FOUND | 404 | 404              | 리소스 없음 |
| CONFLICT | 409 | 409              | 리소스 충돌 |
| INTERNAL_SERVER_ERROR | 500 | 500              | 서버 내부 오류 |
| UNKNOWN_ERROR | 500 | 500              | 알 수 없는 오류 |
