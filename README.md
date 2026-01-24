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
