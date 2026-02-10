# Badge System

## Business Goal
유저별로 특정 조건을 달성했을 때 배지를 지급하는 시스템의 기반을 구축한다. 10개의 배지를 DB에 시드 데이터로 저장하고, 유저와 배지 간의 다대다 연관관계를 설정한다. 배지 이미지는 서버에서 static resource로 제공하며, 배지 조회 및 부여 API를 구현한다.

## Scope
- **In Scope**:
  - Badge 엔티티 (이름, 설명, Tier, 점수, 이미지 URL, 트리거 조건 설명)
  - UserBadge 엔티티 (userId UUID FK + badgeId FK, 획득일시)
  - BadgeTier enum (BEGINNER, INTERMEDIATE, ADVANCED)
  - data.sql로 10개 배지 시드 데이터
  - badge/ 이미지 파일을 src/main/resources/static/badge/로 이동
  - API: 전체 배지 목록 조회, 유저별 보유 배지 조회, 배지 부여
  - BadgeStatus (커스텀 상태 코드 30~39)
- **Out of Scope**:
  - User 엔티티 (userId UUID FK만 보관, 이후 연관관계 매핑)
  - 배지 달성 조건 자동 체크 트리거 로직
  - 배지 달성 알림

## Codebase Analysis Summary
Spring Boot 4.0.1 + Java 25 + JPA + PostgreSQL 프로젝트. 초기 단계로 helloworld 도메인만 존재. Controller-Service-Repository 패턴 사용. 모든 API 응답은 ApiResponse<T>로 자동 래핑. Spotless(Google Java Format AOSP) 적용.

### Relevant Files
| File | Role | Action |
|------|------|--------|
| `badge/enable/*.png`, `badge/disable/*.png` | 배지 이미지 원본 | Move to `src/main/resources/static/badge/` |
| `src/main/java/.../badge/entity/Badge.java` | 배지 엔티티 | Create |
| `src/main/java/.../badge/entity/UserBadge.java` | 유저-배지 연관 엔티티 | Create |
| `src/main/java/.../badge/BadgeTier.java` | 배지 등급 Enum | Create |
| `src/main/java/.../badge/BadgeStatus.java` | 배지 도메인 상태 코드 | Create |
| `src/main/java/.../badge/repository/BadgeRepository.java` | 배지 레포지토리 | Create |
| `src/main/java/.../badge/repository/UserBadgeRepository.java` | 유저배지 레포지토리 | Create |
| `src/main/java/.../badge/service/BadgeService.java` | 배지 서비스 | Create |
| `src/main/java/.../badge/controller/BadgeController.java` | 배지 컨트롤러 | Create |
| `src/main/java/.../badge/dto/BadgeResponse.java` | 배지 응답 DTO | Create |
| `src/main/java/.../badge/dto/UserBadgeResponse.java` | 유저배지 응답 DTO | Create |
| `src/main/java/.../badge/dto/BadgeGrantRequest.java` | 배지 부여 요청 DTO | Create |
| `src/main/java/.../badge/mapper/BadgeMapper.java` | Entity-DTO 변환 | Create |
| `src/main/resources/data.sql` | 배지 시드 데이터 | Create |
| `src/main/resources/application.yml` | JPA ddl-auto 설정 | Modify |
| `README.md` | customStatusCode 범위 기록 | Modify |

### Conventions to Follow
| Convention | Source | Rule |
|-----------|--------|------|
| UUID v7 | BACKEND.md | Entity의 UUID는 uuid.v7() 사용 |
| Enum 파일 분리 | BACKEND.md | Enum은 Inner Class가 아닌 별도 파일 |
| DTO 네이밍 | BACKEND.md | Request/Response 접미사 |
| Mapper 패턴 | BACKEND.md | Entity-DTO 변환은 Mapper 함수 사용 |
| Datetime UTC | BACKEND.md | 항상 UTC 기준 |
| Enum DB 저장 | BACKEND.md | Varchar로 저장, Enum Class로 변환 |
| API 버전 | style_and_conventions | @RequestMapping(version = "0.0.1") |
| 커스텀 상태코드 | style_and_conventions | Badge 도메인: 30~39 범위 |
| Spotless | build.gradle | Google Java Format AOSP |

## Architecture Decisions
| Decision | Choice | Rationale | Alternatives |
|----------|--------|-----------|--------------|
| 배지 저장 방식 | DB 테이블 + data.sql | 이미지URL, 점수, Tier 등 다양한 필드 관리 필요 | Enum only |
| 이미지 서빙 | Spring static resource | src/main/resources/static/ 기본 동작, 별도 설정 불필요 | ResourceHandler 커스텀 |
| User 연관관계 | userId UUID 컬럼만 | User 엔티티 미존재, @ManyToOne 없이 UUID FK만 보관 | JPA 연관관계 |
| 점수 배분 | Tier별 균등 | T1: 8점x5=40, T2: 12점x3=36, T3: 12점x2=24 = 100점 | 개별 지정 |
| 중복 배지 방지 | userId + badgeId unique constraint | 같은 배지 중복 획득 방지 | 없음 |
| BaseEntity | 공통 id, createdAt, updatedAt 추출 | 모든 엔티티가 동일 패턴 사용 | 엔티티별 직접 정의 |

## API Contracts

### GET /api/badges
- Headers: `X-API-Version: 0.0.1`
- Request: 없음
- Response:
```json
{
  "status": { "httpStatusCode": 200, "customStatusCode": 200, "description": "성공" },
  "data": [
    {
      "badgeId": "uuid",
      "name": "빙하 가디언",
      "description": "리소스 절감 여정에 첫발을 내디딘 유저를 환영",
      "tier": "BEGINNER",
      "score": 8,
      "triggerCondition": "서비스 가입 후 첫 번째 프롬프트 교정 및 전송 완료",
      "enableImageUrl": "/badge/enable/enable_gadian.png",
      "disableImageUrl": "/badge/disable/disable_gadian.png"
    }
  ]
}
```
- Note: ApiResponse<T> 자동 래핑

### GET /api/badges/users/{userId}
- Headers: `X-API-Version: 0.0.1`
- Request: Path variable `userId` (UUID)
- Response:
```json
{
  "status": { "httpStatusCode": 200, "customStatusCode": 200, "description": "성공" },
  "data": [
    {
      "badgeId": "uuid",
      "name": "빙하 가디언",
      "description": "리소스 절감 여정에 첫발을 내디딘 유저를 환영",
      "tier": "BEGINNER",
      "score": 8,
      "enableImageUrl": "/badge/enable/enable_gadian.png",
      "disableImageUrl": "/badge/disable/disable_gadian.png",
      "earnedAt": "2026-02-09T00:00:00Z"
    }
  ]
}
```
- Note: 유저가 보유한 배지 목록 반환

### POST /api/badges/users/{userId}/grant
- Headers: `X-API-Version: 0.0.1`
- Request:
```json
{
  "badgeId": "uuid"
}
```
- Response:
```json
{
  "status": { "httpStatusCode": 200, "customStatusCode": 200, "description": "성공" },
  "data": {
    "badgeId": "uuid",
    "name": "빙하 가디언",
    "earnedAt": "2026-02-09T00:00:00Z"
  }
}
```
- Note: 이미 보유한 배지면 BADGE_ALREADY_EARNED(409, 31) 에러

## Data Models

### Badge
| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID (v7) | PK |
| name | String | NOT NULL, UNIQUE |
| description | String | NOT NULL |
| tier | BadgeTier (VARCHAR) | NOT NULL |
| score | Integer | NOT NULL |
| triggerCondition | String | NOT NULL |
| enableImageUrl | String | NOT NULL |
| disableImageUrl | String | NOT NULL |
| createdAt | Instant | NOT NULL, UTC |
| updatedAt | Instant | NOT NULL, UTC |

### UserBadge
| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID (v7) | PK |
| userId | UUID | NOT NULL, FK(미래 User) |
| badgeId | UUID | NOT NULL, FK(Badge) |
| earnedAt | Instant | NOT NULL, UTC |
| createdAt | Instant | NOT NULL, UTC |
| updatedAt | Instant | NOT NULL, UTC |
| | | UNIQUE(userId, badgeId) |

### BadgeTier (Enum)
- BEGINNER
- INTERMEDIATE
- ADVANCED

## Implementation Todos

### Todo 1: 배지 이미지 파일 이동
- **Priority**: 1
- **Dependencies**: none
- **Goal**: badge/ 이미지 파일을 Spring static resource 디렉토리로 이동
- **Work**:
  - `badge/enable/` -> `src/main/resources/static/badge/enable/`로 이동
  - `badge/disable/` -> `src/main/resources/static/badge/disable/`로 이동
  - 루트의 `badge/` 디렉토리 삭제
- **Convention Notes**: Spring Boot 기본 static resource 경로 활용
- **Verification**: `ls src/main/resources/static/badge/enable/` 및 `disable/`로 파일 존재 확인
- **Exit Criteria**: 20개 이미지 파일(enable 10, disable 10)이 static 디렉토리에 존재
- **Status**: completed

### Todo 2: BaseEntity 및 BadgeTier Enum 생성
- **Priority**: 1
- **Dependencies**: none
- **Goal**: 공통 엔티티 베이스 클래스와 배지 등급 Enum 생성
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/common/entity/BaseEntity.java` 생성
    - `@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`
    - `id` (UUID v7, `@GeneratedValue(strategy = GenerationType.UUID)`)
    - `createdAt` (`@CreatedDate`, Instant, UTC)
    - `updatedAt` (`@LastModifiedDate`, Instant, UTC)
  - `src/main/java/com/rokyai/dnd14th1backend/badge/BadgeTier.java` 생성
    - enum 값: BEGINNER, INTERMEDIATE, ADVANCED
  - `src/main/java/com/rokyai/dnd14th1backend/Dnd14th1BackendApplication.java`에 `@EnableJpaAuditing` 추가
  - `src/main/resources/application.yml`에 JPA ddl-auto: update 설정 추가
- **Convention Notes**: Enum은 별도 파일, UTC datetime, UUID v7
- **Verification**: 빌드 성공 (`./gradlew compileJava`)
- **Exit Criteria**: BaseEntity, BadgeTier 컴파일 성공
- **Status**: completed (기존 User 엔티티 패턴에 맞춰 @CreationTimestamp/@UpdateTimestamp + LocalDateTime 사용, @EnableJpaAuditing 이미 존재)

### Todo 3: Badge 엔티티 생성
- **Priority**: 2
- **Dependencies**: Todo 2
- **Goal**: 배지 정보를 저장하는 JPA 엔티티 생성
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/badge/entity/Badge.java` 생성
    - BaseEntity 상속
    - 필드: name(String, unique), description(String), tier(BadgeTier, `@Enumerated(STRING)`), score(Integer), triggerCondition(String), enableImageUrl(String), disableImageUrl(String)
    - `@Table(name = "badge")`
    - Lombok `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`
- **Convention Notes**: Enum은 VARCHAR로 저장 (@Enumerated(STRING)), camelCase 필드는 Spring Boot 기본 snake_case 매핑
- **Verification**: 빌드 성공
- **Exit Criteria**: Badge 엔티티 컴파일 성공, 테이블 DDL 생성 가능
- **Status**: completed

### Todo 4: UserBadge 엔티티 생성
- **Priority**: 2
- **Dependencies**: Todo 2, Todo 3
- **Goal**: 유저-배지 연관관계 엔티티 생성
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/badge/entity/UserBadge.java` 생성
    - BaseEntity 상속
    - 필드: userId(UUID, NOT NULL), earnedAt(Instant, NOT NULL)
    - Badge와 @ManyToOne 연관관계 (`@JoinColumn(name = "badge_id")`)
    - `@Table(name = "user_badge", uniqueConstraints = @UniqueConstraint(columns = {"user_id", "badge_id"}))`
    - Lombok `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`
- **Convention Notes**: userId는 UUID 컬럼만 (User 엔티티 없이), unique constraint로 중복 방지
- **Verification**: 빌드 성공
- **Exit Criteria**: UserBadge 엔티티 컴파일 성공
- **Status**: completed

### Todo 5: data.sql 배지 시드 데이터 생성
- **Priority**: 2
- **Dependencies**: Todo 3
- **Goal**: 10개 배지를 DB에 하드코딩
- **Work**:
  - `src/main/resources/data.sql` 생성
  - 10개 배지 INSERT 문 작성 (ON CONFLICT DO NOTHING으로 멱등성 보장)
  - 배지 목록 및 점수:
    - Tier1 (각 8점): 빙하 가디언(gadian), 원샷 패스(oneshot), 에코 그리팅(ecogriting), 슬림 프롬프트(slimpt), 빙하의 발자국(foot)
    - Tier2 (각 12점): 탄소 킬러(killer), 제로 웨이스트(zeroweist), 빙하 복구사(icebergheal)
    - Tier3 (각 12점): 북극의 심장(heart), 절대영도 마스터(master)
  - application.yml에 `spring.jpa.defer-datasource-initialization: true`, `spring.sql.init.mode: always` 추가
- **Convention Notes**: 이미지 URL은 `/badge/enable/enable_{slug}.png` 패턴
- **Verification**: 앱 기동 후 data.sql 실행 성공 확인
- **Exit Criteria**: DB에 10개 배지 레코드 존재
- **Status**: completed

### Todo 6: BadgeStatus 커스텀 상태 코드 생성
- **Priority**: 1
- **Dependencies**: none
- **Goal**: 배지 도메인 전용 상태 코드 정의
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/badge/BadgeStatus.java` 생성
    - StatusInterface 구현
    - BADGE_NOT_FOUND(404, 30, "배지를 찾을 수 없습니다")
    - BADGE_ALREADY_EARNED(409, 31, "이미 획득한 배지입니다")
- **Convention Notes**: customStatusCode 30~39 범위, StatusInterface 구현
- **Verification**: 빌드 성공
- **Exit Criteria**: BadgeStatus 컴파일 성공
- **Status**: completed

### Todo 7: Repository 계층 생성
- **Priority**: 2
- **Dependencies**: Todo 3, Todo 4
- **Goal**: Badge, UserBadge JPA Repository 생성
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/badge/repository/BadgeRepository.java` 생성
    - `JpaRepository<Badge, UUID>` 상속
  - `src/main/java/com/rokyai/dnd14th1backend/badge/repository/UserBadgeRepository.java` 생성
    - `JpaRepository<UserBadge, UUID>` 상속
    - `List<UserBadge> findByUserId(UUID userId)`
    - `boolean existsByUserIdAndBadge_Id(UUID userId, UUID badgeId)`
- **Convention Notes**: Spring Data JPA 쿼리 메서드 네이밍 컨벤션
- **Verification**: 빌드 성공
- **Exit Criteria**: 두 Repository 컴파일 성공
- **Status**: completed

### Todo 8: DTO 및 Mapper 생성
- **Priority**: 2
- **Dependencies**: Todo 3, Todo 4
- **Goal**: API 응답/요청 DTO와 Entity-DTO 변환 Mapper 생성
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/badge/dto/BadgeResponse.java` 생성
    - badgeId, name, description, tier, score, triggerCondition, enableImageUrl, disableImageUrl
    - record 타입 사용
  - `src/main/java/com/rokyai/dnd14th1backend/badge/dto/UserBadgeResponse.java` 생성
    - badgeId, name, description, tier, score, enableImageUrl, disableImageUrl, earnedAt
    - record 타입 사용
  - `src/main/java/com/rokyai/dnd14th1backend/badge/dto/BadgeGrantRequest.java` 생성
    - badgeId (UUID, @NotNull)
    - record 타입 사용
  - `src/main/java/com/rokyai/dnd14th1backend/badge/dto/BadgeGrantResponse.java` 생성
    - badgeId, name, earnedAt
    - record 타입 사용
  - `src/main/java/com/rokyai/dnd14th1backend/badge/mapper/BadgeMapper.java` 생성
    - `static BadgeResponse toResponse(Badge badge)`
    - `static UserBadgeResponse toUserBadgeResponse(UserBadge userBadge)`
    - `static BadgeGrantResponse toGrantResponse(UserBadge userBadge)`
- **Convention Notes**: DTO는 record, Request/Response 접미사, Mapper는 static 메서드, Description 필수
- **Verification**: 빌드 성공
- **Exit Criteria**: DTO, Mapper 컴파일 성공
- **Status**: completed

### Todo 9: BadgeService 생성
- **Priority**: 3
- **Dependencies**: Todo 6, Todo 7, Todo 8
- **Goal**: 배지 비즈니스 로직 구현
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/badge/service/BadgeService.java` 생성
    - `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)`
    - `List<BadgeResponse> getAllBadges()` - 전체 배지 목록 조회
    - `List<UserBadgeResponse> getUserBadges(UUID userId)` - 유저 보유 배지 조회
    - `@Transactional BadgeGrantResponse grantBadge(UUID userId, BadgeGrantRequest request)` - 배지 부여
      - badgeId로 Badge 조회 (없으면 BADGE_NOT_FOUND)
      - userId + badgeId 중복 확인 (있으면 BADGE_ALREADY_EARNED)
      - UserBadge 생성 및 저장
- **Convention Notes**: 읽기 전용 메서드는 클래스 레벨 readOnly, 쓰기 메서드만 @Transactional 오버라이드
- **Verification**: 빌드 성공
- **Exit Criteria**: BadgeService 컴파일 성공
- **Status**: completed

### Todo 10: BadgeController 생성
- **Priority**: 4
- **Dependencies**: Todo 9
- **Goal**: 배지 REST API 엔드포인트 구현
- **Work**:
  - `src/main/java/com/rokyai/dnd14th1backend/badge/controller/BadgeController.java` 생성
    - `@RestController`, `@RequiredArgsConstructor`
    - `@RequestMapping(version = "0.0.1", path = "/api/badges")`
    - `@GetMapping` getAllBadges() - 전체 배지 목록
    - `@GetMapping("/users/{userId}")` getUserBadges(@PathVariable UUID userId) - 유저 배지 조회
    - `@PostMapping("/users/{userId}/grant")` grantBadge(@PathVariable UUID userId, @Valid @RequestBody BadgeGrantRequest request) - 배지 부여
    - Swagger 어노테이션: @Operation, @ApiResponse (에러케이스 포함)
- **Convention Notes**: API Description 한국어, Swagger에 모든 Exception Case 명시, version = "0.0.1"
- **Verification**: 빌드 성공 + Spotless 통과 (`./gradlew spotlessCheck`)
- **Exit Criteria**: 전체 프로젝트 빌드 및 Spotless 통과
- **Status**: completed

### Todo 11: 최종 검증 및 Spotless 적용
- **Priority**: 5
- **Dependencies**: Todo 1, Todo 5, Todo 10
- **Goal**: 전체 코드 포맷팅 및 빌드 검증
- **Work**:
  - `./gradlew spotlessApply` 실행하여 코드 포맷팅
  - `./gradlew compileJava` 빌드 성공 확인
  - README.md에 Badge 도메인 customStatusCode 범위(30~39) 기록
- **Convention Notes**: Spotless Google Java Format AOSP
- **Verification**: `./gradlew spotlessCheck` 및 `./gradlew compileJava` 통과
- **Exit Criteria**: 빌드 성공, Spotless 통과, README 업데이트
- **Status**: completed

## Verification Strategy
- `./gradlew spotlessApply` - 코드 포맷팅 적용
- `./gradlew compileJava` - 전체 컴파일 성공 확인
- `./gradlew spotlessCheck` - 포맷팅 검증
- static 이미지 파일 20개 존재 확인
- data.sql 시드 데이터 10개 배지 INSERT 확인

## Progress Tracking
- Total Todos: 11
- Completed: 11
- Status: Execution complete

## Change Log
- 2026-02-09: Plan created
- 2026-02-09: All todos executed. CustomStatusCode 4000~4999 (not 30~39 as originally planned, aligned with 1000-unit convention). BaseEntity uses @CreationTimestamp/@UpdateTimestamp + LocalDateTime (matching existing User entity pattern).
