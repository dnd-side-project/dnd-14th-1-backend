# DND 14th 1조 Backend

## CORS Configuration

### 개요
프론트엔드 애플리케이션에서 백엔드 API에 접근할 수 있도록 CORS(Cross-Origin Resource Sharing)를 설정합니다.

### 설정 방법

`.env` 파일에서 허용할 오리진을 쉼표로 구분하여 지정합니다:

```env
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://localhost.com,https://example.com
```

### 기본값
```
http://localhost:3000
```

### 허용되는 메서드 및 헤더
- **메서드**: GET, POST, PUT, DELETE, PATCH, OPTIONS
- **헤더**: 모든 헤더 허용 (*)
- **자격증명**: 허용 (credentials: true)
- **캐시 시간**: 3600초 (1시간)

### 예시

**개발 환경**
```env
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://localhost.com
```

**프로덕션 환경**
```env
CORS_ALLOWED_ORIGINS=https://example.com,https://www.example.com
```

## Local HTTPS Setup (Mac)

Apple OAuth 등 HTTPS가 필요한 기능을 로컬에서 테스트하기 위한 SSL 설정 방법입니다.

### 사전 요구사항
- [Homebrew](https://brew.sh) (mkcert 자동 설치에 필요)

### 설정 방법

```bash
# 스크립트 실행 권한 부여
chmod +x scripts/macos_local_ssh_installer.sh

# 스크립트 실행
./scripts/macos_local_ssh_installer.sh
```

스크립트가 수행하는 작업:
1. `mkcert` 설치 (미설치 시 Homebrew로 자동 설치)
2. 로컬 CA(인증 기관) 설치
3. `conf/ssl/` 디렉토리에 SSL 인증서 생성 (`localhost`, `127.0.0.1`, `::1`)
4. Spring Boot용 PKCS12 키스토어(`keystore.p12`)로 변환

### .env 설정

스크립트 실행 후 `.env` 파일에 다음을 추가합니다:

```env
SERVER_SSL_ENABLED=true
SERVER_SSL_KEYSTORE_PATH=conf/ssl/keystore.p12
SERVER_SSL_KEYSTORE_PASSWORD=password
SERVER_SSL_KEYSTORE_TYPE=PKCS12
SERVER_SSL_KEY_ALIAS=tomcat
SERVER_PORT=8443
```

### 서버 실행

```bash
./gradlew bootRun
```

설정 완료 후 `https://localhost:8443`으로 접근할 수 있습니다.

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
| `DefaultStatus` | 기본 상태 코드 Enum (0~9, 9000~9999 범위) |
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
throw new ApiException(AuthStatus.INVALID_REFRESH_TOKEN);
```

**도메인별 Status 정의**
```java
@Getter
@RequiredArgsConstructor
public enum AuthStatus implements StatusInterface {
    // 잘못된 요청 (1400)
    INVALID_ID_TOKEN(400, 1400, "유효하지 않은 ID Token입니다"),

    // 인증 실패 (1401~1404)
    INVALID_OAUTH_REQUEST(400, 1401, "유효하지 않은 OAuth 요청입니다"),
    INVALID_REFRESH_TOKEN(401, 1402, "유효하지 않은 Refresh Token입니다"),
    EXPIRED_TOKEN(401, 1403, "만료된 토큰입니다"),
    INVALID_ISSUER(401, 1404, "유효하지 않은 issuer입니다"),

    // 권한 없음 (1405)
    INVALID_AUDIENCE(403, 1405, "유효하지 않은 audience입니다");

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
| 1000~1999 | Auth 도메인 |
| 2000~2999 | Order 도메인 |
| 3000~3999 | User 도메인 |
| 9000~9999 | 서버 에러 |

### 기본 상태 코드 (DefaultStatus)

| 상태 | HTTP | customStatusCode | 설명 |
|-----|------|------------------|------|
| OK | 200 | 0                | 성공 |
| BAD_REQUEST | 400 | 1                | 잘못된 요청 |
| UNAUTHORIZED | 401 | 2                | 인증 필요 |
| FORBIDDEN | 403 | 3                | 접근 권한 없음 |
| NOT_FOUND | 404 | 4                | 리소스 없음 |
| CONFLICT | 409 | 5                | 리소스 충돌 |
| INTERNAL_SERVER_ERROR | 500 | 9000             | 서버 내부 오류 |
| UNKNOWN_ERROR | 500 | 9001             | 알 수 없는 오류 |
