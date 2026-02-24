# User Me API + Apple OAuth name/email 추가

## Business Goal
클라이언트(Flutter 앱)에서 현재 로그인한 사용자 정보를 조회할 수 있는 User Me API를 제공하고,
Apple OAuth 최초 로그인 시 사용자 이름과 이메일을 서버로 전달받아 저장할 수 있도록 한다.
대표 배지 정보도 함께 응답에 포함하여 프로필 화면에서 활용할 수 있게 한다.

## Scope
- **In Scope**:
  - `User` 엔티티에 `name` 필드 추가 (nullable)
  - `AppleOAuthRequest`에 `name`, `email` optional 필드 추가
  - `AppleOAuthService` 수정: 클라이언트 전달 name/email 우선 사용
  - `UserMeResponse` DTO 생성 (대표 배지 포함)
  - `UserController` + `UserService` 생성 (GET /api/v1/users/me)
  - `UserMapper` 생성 (User → UserMeResponse 변환)
  - Flyway 마이그레이션 (users 테이블에 name 컬럼 추가)
- **Out of Scope**:
  - 기존 JWT 인증 흐름 변경
  - 테스트 코드 작성
  - 다른 OAuth 프로바이더

## Codebase Analysis Summary
- Spring Boot + JPA 기반, Controller-Service-Repository 계층 구조
- 인증: `@AuthenticationPrincipal UUID userId`로 SecurityContext에서 사용자 ID 추출
- 응답 자동 래핑: `ApiResponseWrapper`가 `ApiResponse<T>`로 감싸줌
- DTO는 Java record 사용 (BadgeResponse, EarnedBadgeInfo 등)
- Mapper는 static 메서드 기반 유틸 클래스 (BadgeMapper 패턴)
- `/api/v1/**` 경로는 인증 필수 (SecurityConfig)
- `/open-api/**` 경로는 인증 불필요

### Relevant Files
| File | Role | Action |
|------|------|--------|
| `users/domain/User.java` | 사용자 엔티티 | Modify — name 필드 추가 |
| `auth/dto/AppleOAuthRequest.java` | Apple OAuth 요청 DTO | Modify — name, email 필드 추가 |
| `auth/service/AppleOAuthService.java` | Apple OAuth 로그인 서비스 | Modify — 클라이언트 name/email 우선 사용 |
| `users/dto/UserMeResponse.java` | User Me 응답 DTO | Create |
| `users/dto/RepresentativeBadgeResponse.java` | 대표 배지 응답 DTO | Create |
| `users/controller/UserController.java` | 사용자 API 컨트롤러 | Create |
| `users/service/UserService.java` | 사용자 서비스 | Create |
| `users/mapper/UserMapper.java` | User Entity-DTO 변환 | Create |
| `users/infrastructure/UserRepository.java` | User 저장소 | Reference |
| `badge/entity/Badge.java` | 배지 엔티티 | Reference |
| `badge/entity/UserBadge.java` | 유저-배지 연관 엔티티 | Reference |

### Conventions to Follow
| Convention | Source | Rule |
|-----------|--------|------|
| DTO record | `BadgeResponse.java` | Java record 사용, `@Schema` 어노테이션 필수 |
| Mapper | `BadgeMapper.java` | static 메서드 기반, private 생성자, final 클래스 |
| Controller | `UserBadgeController.java` | `@Tag`, `@Operation`, `@ApiResponses` Swagger 어노테이션 |
| Auth | `UserBadgeController.java` | `@AuthenticationPrincipal UUID userId` |
| Response naming | BACKEND.md | `~Response`로 끝남, Description 필수 |
| Status codes | `style_and_conventions` | User 도메인: 3000~3999 |
| JavaDoc | `CODE_PRINCIPLES.md` | 한국어 주석, @param/@return 비즈니스 역할 명시 |

## Architecture Decisions
| Decision | Choice | Rationale | Alternatives |
|----------|--------|-----------|--------------|
| name 필드 위치 | `User` 엔티티 | 사용자 프로필 핵심 속성 | UserIdentity (부적절) |
| API 경로 | `GET /api/v1/users/me` | RESTful 관례, 기존 패턴 | `/api/v1/me` |
| 컨트롤러 | 새 `UserController` | UserGameController는 게임 프로필 전용 | 기존 컨트롤러에 추가 |
| 대표 배지 응답 | 별도 nested DTO | 관심사 분리, 재사용 가능 | flat 필드로 펼치기 |
| name/email 우선순위 | 클라이언트 전달 값 > Apple payload | Apple은 최초 인증 후 이메일 미제공 가능 | payload만 사용 |

## API Contracts

### GET /api/v1/users/me
- Headers: `Authorization: Bearer {accessToken}`
- Request: 없음
- Response:
```json
{
  "userId": "uuid",
  "email": "string | null",
  "name": "string | null",
  "createdAt": "datetime",
  "representativeBadge": {
    "badgeId": "uuid",
    "name": "string",
    "tier": "BRONZE | SILVER | GOLD | ...",
    "enableImageUrl": "string",
    "disableImageUrl": "string"
  } | null
}
```
- Note: `representativeBadge`는 대표 배지를 설정하지 않은 경우 null

### POST /open-api/v1/auth/apple (수정)
- Request에 optional 필드 추가:
```json
{
  "deviceId": "string (required)",
  "platform": "IOS (required)",
  "packageName": "string (required)",
  "idToken": "string (required)",
  "name": "string (optional)",
  "email": "string (optional)"
}
```

## Data Models

### User (수정)
| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK, uuid_v7 |
| name | String | nullable, 추가 |
| email | String | nullable |
| representative_user_badge_id | UUID | FK → user_badge.id, nullable |
| created_at | LocalDateTime | not null |
| updated_at | LocalDateTime | not null |

## Implementation Todos

### Todo 1: User 엔티티에 name 필드 추가
- **Priority**: 1
- **Dependencies**: none
- **Goal**: User 엔티티에 사용자 이름을 저장할 수 있도록 name 필드를 추가한다
- **Work**:
  - `users/domain/User.java`에 `private String name` 필드 추가 (`@Column(nullable = true)`)
  - `email` 필드 아래에 배치
  - `User.create()` 정적 팩토리 메서드에 `name` 파라미터 추가
  - Flyway 마이그레이션 SQL 파일 생성: `ALTER TABLE users ADD COLUMN name VARCHAR(255)`
- **Convention Notes**: 기존 필드 스타일 일치, nullable 컬럼
- **Verification**: 빌드 성공
- **Exit Criteria**: User 엔티티에 name 필드가 존재하고 빌드 통과
- **Status**: pending

### Todo 2: AppleOAuthRequest에 name/email 필드 추가
- **Priority**: 1
- **Dependencies**: none
- **Goal**: Apple OAuth 로그인 요청에 선택적으로 사용자 이름과 이메일을 전달할 수 있도록 한다
- **Work**:
  - `auth/dto/AppleOAuthRequest.java`에 `name` 필드 추가 (optional, `@Schema` 포함)
  - `auth/dto/AppleOAuthRequest.java`에 `email` 필드 추가 (optional, `@Schema` 포함)
  - 두 필드 모두 `@NotBlank` 없이 nullable로 설정
- **Convention Notes**: 기존 필드 스타일과 동일한 `@Schema` 어노테이션
- **Verification**: 빌드 성공
- **Exit Criteria**: AppleOAuthRequest에 optional name, email 필드 존재
- **Status**: pending

### Todo 3: AppleOAuthService 수정 — 클라이언트 name/email 사용
- **Priority**: 2
- **Dependencies**: Todo 1, Todo 2
- **Goal**: 신규 사용자 생성 시 클라이언트가 전달한 name/email을 우선 사용하도록 수정한다
- **Work**:
  - `auth/service/AppleOAuthService.java`의 `authenticateWithApple()` 메서드 수정
  - `createNewUser()` 메서드 시그니처 변경: `AppleOAuthRequest request` 파라미터 추가
  - 이메일: `request.getEmail() != null ? request.getEmail() : payload.getEmail()`
  - 이름: `request.getName()` 사용 (Apple payload에는 name 없음)
  - `User.create()` 호출 시 name도 전달
- **Convention Notes**: null-safe 처리, 기존 메서드 스타일 유지
- **Verification**: 빌드 성공
- **Exit Criteria**: 클라이언트 전달 name/email이 우선 사용되고, 없으면 payload 값 fallback
- **Status**: pending

### Todo 4: User Me 응답 DTO 생성
- **Priority**: 1
- **Dependencies**: none
- **Goal**: User Me API의 응답 DTO를 생성한다
- **Work**:
  - `users/dto/RepresentativeBadgeResponse.java` 생성 (record)
    - 필드: `badgeId`, `name`, `tier`, `enableImageUrl`, `disableImageUrl`
    - 각 필드에 `@Schema(description = "...")` 추가
  - `users/dto/UserMeResponse.java` 생성 (record)
    - 필드: `userId`, `email`, `name`, `createdAt`, `representativeBadge`(RepresentativeBadgeResponse, nullable)
    - 각 필드에 `@Schema(description = "...")` 추가
- **Convention Notes**: Java record, `@Schema` 어노테이션, Response suffix
- **Verification**: 빌드 성공
- **Exit Criteria**: 두 DTO record가 컴파일되고 Swagger에 스키마 표시
- **Status**: pending

### Todo 5: UserMapper 생성
- **Priority**: 2
- **Dependencies**: Todo 1, Todo 4
- **Goal**: User 엔티티를 UserMeResponse로 변환하는 Mapper를 생성한다
- **Work**:
  - `users/mapper/UserMapper.java` 생성
  - `private UserMapper()` 생성자 (유틸 클래스)
  - `public static UserMeResponse toMeResponse(User user)` 메서드
    - User의 representativeUserBadge가 null이 아니면 RepresentativeBadgeResponse 생성
    - null이면 representativeBadge를 null로 설정
- **Convention Notes**: `BadgeMapper` 패턴 따름 — final 클래스, static 메서드, private 생성자
- **Verification**: 빌드 성공
- **Exit Criteria**: UserMapper.toMeResponse()가 User → UserMeResponse 변환 정상 동작
- **Status**: pending

### Todo 6: UserService 생성
- **Priority**: 2
- **Dependencies**: Todo 5
- **Goal**: User Me API의 비즈니스 로직을 처리하는 서비스를 생성한다
- **Work**:
  - `users/service/UserService.java` 생성
  - `@Service`, `@Transactional(readOnly = true)` 어노테이션
  - `UserRepository` 주입
  - `public UserMeResponse getMe(UUID userId)` 메서드
    - `userRepository.findById(userId)` → orElseThrow
    - `UserMapper.toMeResponse(user)` 반환
  - 사용자 미존재 시 예외: 기존 `UserGameErrorStatus` 참고하여 적절한 에러 처리
- **Convention Notes**: 기존 Service 패턴, 생성자 주입
- **Verification**: 빌드 성공
- **Exit Criteria**: UserService.getMe()가 userId로 사용자 조회 후 UserMeResponse 반환
- **Status**: pending

### Todo 7: UserController 생성
- **Priority**: 2
- **Dependencies**: Todo 6
- **Goal**: GET /api/v1/users/me 엔드포인트를 생성한다
- **Work**:
  - `users/controller/UserController.java` 생성
  - `@RestController`, `@RequestMapping("/api/v1/users")`, `@Tag(name = "사용자")`
  - `UserService` 주입
  - `@GetMapping("/me")` 메서드
    - `@Operation(summary = "내 정보 조회")`
    - `@ApiResponses` — 200 성공, 401 인증 실패
    - `@AuthenticationPrincipal UUID userId` 파라미터
    - `userService.getMe(userId)` 호출
- **Convention Notes**: 기존 `UserGameController`, `UserBadgeController` 패턴 따름
- **Verification**: 빌드 성공, Swagger UI에서 엔드포인트 확인
- **Exit Criteria**: Swagger에 GET /api/v1/users/me 표시되고 인증 후 정상 응답
- **Status**: pending

## Verification Strategy
- `./gradlew build` — 전체 빌드 성공
- `./gradlew spotlessCheck` — 코드 포맷 검증
- Swagger UI에서 User Me API 엔드포인트 확인 가능

## Progress Tracking
- Total Todos: 7
- Completed: 0
- Status: Planning complete

## Change Log
- 2026-02-24: Plan created
