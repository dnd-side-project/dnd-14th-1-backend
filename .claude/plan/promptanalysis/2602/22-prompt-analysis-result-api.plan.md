# Prompt Analysis Result API

## Business Goal
iOS 앱에서 Apple Intelligence (on-device FoundationModels)로 수행한 프롬프트 최적화 결과를 백엔드에 제출하고, 결과를 DB에 저장하며, 기존 배지 시스템을 통해 배지를 발급하는 API를 구현한다.

## Scope
- **In Scope**: PromptAnalysisResult/PromptAnalysisSuggestion 엔티티, Request/Response DTO, Controller, Service, Repository, Mapper, Exception, DB 마이그레이션
- **Out of Scope**: 새 배지 트리거 타입 추가, iOS 앱 코드 변경, 결과 조회/삭제 API

## Codebase Analysis Summary
- 기존 `optimizeChat` 흐름: `ConversationController` → `UserGameService.optimizeChat()` → XP 적립 + `BadgeEventService.checkBadgesOnOptimize()`
- 배지 체크 메서드는 숫자값만 받으므로 재사용 가능: `checkBadgesOnOptimize(userId, tokenSaving, totalXp, optimizeCount)`
- `OPTIMIZE_COUNT` 배지는 `chatRepository.countOptimizedByUserId()`로 카운트 → intelligence 결과도 합산 필요

### Relevant Files
| File | Role | Action |
|------|------|--------|
| `promptanalysis/domain/PromptAnalysisResult.java` | 분석 결과 엔티티 | Create |
| `promptanalysis/domain/PromptAnalysisSuggestion.java` | 개선 제안 엔티티 | Create |
| `promptanalysis/infrastructure/PromptAnalysisResultRepository.java` | 결과 리포지토리 | Create |
| `promptanalysis/dto/PromptAnalysisResultRequest.java` | 요청 DTO | Create |
| `promptanalysis/dto/PromptAnalysisResultResponse.java` | 응답 DTO | Create |
| `promptanalysis/dto/PromptSuggestionRequest.java` | 제안 요청 DTO | Create |
| `promptanalysis/mapper/PromptAnalysisMapper.java` | Entity↔DTO 변환 | Create |
| `promptanalysis/controller/PromptAnalysisController.java` | API 컨트롤러 | Create |
| `promptanalysis/service/PromptAnalysisService.java` | 비즈니스 로직 | Create |
| `promptanalysis/exception/PromptAnalysisErrorStatus.java` | 에러 코드 | Create |
| `promptanalysis/exception/PromptAnalysisException.java` | 도메인 예외 | Create |
| `users/service/UserGameService.java` | XP/티어 계산 메서드 | Reference (calculateTier, calculateProgress 재사용) |
| `badge/service/BadgeEventService.java` | 배지 체크 | Reference (checkBadgesOnOptimize 호출) |

### Conventions to Follow
| Convention | Source | Rule |
|-----------|--------|------|
| DTO 명명 | BACKEND.md | Request/Response 접미사 필수 |
| Record 기반 DTO | 기존 코드 패턴 | `public record` 사용, `@Schema` 포함 |
| Mapper 패턴 | BadgeMapper | static 메서드, `final class`, private 생성자 |
| UUID PK | Badge, Chat 등 | `@UuidV7` 사용 |
| Swagger | BACKEND.md | `@Tag`, `@Operation`, `@ApiResponses` 필수, 한국어 설명 |
| 에러 코드 | style_and_conventions | 4000~4999 범위 (promptanalysis 도메인) |
| 생성자 주입 | CrawlingController | 명시적 생성자 주입 (Lombok `@RequiredArgsConstructor` 또는 직접 작성) |

## Architecture Decisions
| Decision | Choice | Rationale | Alternatives |
|----------|--------|-----------|--------------|
| 도메인 패키지 | `promptanalysis` | 독립적 기능, 사용자 승인 | `users`에 추가 |
| Suggestion 저장 | JPA `@OneToMany` cascade | 정규화, 개별 조회 가능 | JSON 컬럼 |
| XP 계산 | 기존 `XP_PER_TOKEN * tokenSaving` 공식 재사용 | 일관성 | 별도 공식 |
| optimizeCount | chat 최적화 + intelligence 합산 | 통합 배지 관리 | 별도 카운트 |
| noImprovement/cannotImprove | 해당 경우 XP/배지 미적립, 결과만 저장 | 무의미한 결과에 보상 방지 | 클라이언트 필터링 |

## API Contracts

### POST /api/v1/prompt-analysis/results
- Headers: `Authorization: Bearer {token}`
- Request:
```json
{
  "suggestions": [
    {
      "originalPart": "string",
      "improvedPart": "string",
      "reason": "string"
    }
  ],
  "estimatedTokenSaving": 150,
  "glacierMeltReductionKg": 0.0018,
  "noImprovement": false,
  "cannotImprove": false
}
```
- Response:
```json
{
  "resultId": "uuid",
  "xpEarned": 180,
  "totalXp": 5000,
  "tier": 3,
  "progress": 0.45,
  "earnedBadges": [
    {
      "badgeId": "uuid",
      "name": "string",
      "tier": "BEGINNER",
      "enableImageUrl": "string",
      "disableImageUrl": "string",
      "earnedAt": "2026-02-22T12:00:00"
    }
  ]
}
```
- Note: `noImprovement=true` 또는 `cannotImprove=true`일 경우 xpEarned=0, earnedBadges=[]

## Data Models

### PromptAnalysisResult
| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK, @UuidV7 |
| userId | UUID | NOT NULL |
| estimatedTokenSaving | Integer | NOT NULL |
| glacierMeltReductionKg | Double | NOT NULL |
| noImprovement | Boolean | NOT NULL |
| cannotImprove | Boolean | NOT NULL |
| xpEarned | Integer | NOT NULL |
| createdAt | LocalDateTime | NOT NULL, auto |
| updatedAt | LocalDateTime | NOT NULL, auto |

### PromptAnalysisSuggestion
| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK, @UuidV7 |
| result | PromptAnalysisResult | FK, NOT NULL |
| originalPart | String (TEXT) | NOT NULL |
| improvedPart | String (TEXT) | NOT NULL |
| reason | String (TEXT) | NOT NULL |
| createdAt | LocalDateTime | NOT NULL, auto |

## Implementation Todos

### Todo 1: Entity 및 Repository 생성
- **Priority**: 1
- **Dependencies**: none
- **Goal**: PromptAnalysisResult, PromptAnalysisSuggestion 엔티티와 리포지토리를 생성한다
- **Work**:
  - `promptanalysis/domain/PromptAnalysisResult.java`: Entity 클래스 (위 Data Model 참조), `@OneToMany(cascade = ALL, orphanRemoval = true)` 관계
  - `promptanalysis/domain/PromptAnalysisSuggestion.java`: Entity 클래스, `@ManyToOne(fetch = LAZY)` 관계
  - `promptanalysis/infrastructure/PromptAnalysisResultRepository.java`: `JpaRepository<PromptAnalysisResult, UUID>` + `countByUserId(UUID userId)` 메서드
- **Convention Notes**: `@UuidV7`, `@CreationTimestamp`/`@UpdateTimestamp`, `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`
- **Verification**: 빌드 성공
- **Exit Criteria**: 엔티티와 리포지토리가 생성되고 빌드 통과
- **Status**: pending

### Todo 2: Exception 클래스 생성
- **Priority**: 1
- **Dependencies**: none
- **Goal**: promptanalysis 도메인의 에러 코드와 예외 클래스를 생성한다
- **Work**:
  - `promptanalysis/exception/PromptAnalysisErrorStatus.java`: `StatusInterface` 구현, 4000~4999 범위
    - `INVALID_ANALYSIS_RESULT(400, 4400, "유효하지 않은 분석 결과입니다.")`
  - `promptanalysis/exception/PromptAnalysisException.java`: `ApiException` 확장
- **Convention Notes**: 기존 `UserGameException`/`CrawlingException` 패턴 따름
- **Verification**: 빌드 성공
- **Exit Criteria**: 예외 클래스가 생성되고 빌드 통과
- **Status**: pending

### Todo 3: DTO 및 Mapper 생성
- **Priority**: 1
- **Dependencies**: none
- **Goal**: Request/Response DTO와 Mapper를 생성한다
- **Work**:
  - `promptanalysis/dto/PromptSuggestionRequest.java`: record, `@Schema` 포함
  - `promptanalysis/dto/PromptAnalysisResultRequest.java`: record, `@Valid`, `@NotNull`, `@Min`/`@Max` 검증 포함
  - `promptanalysis/dto/PromptAnalysisResultResponse.java`: record, resultId + xpEarned + totalXp + tier + progress + earnedBadges
  - `promptanalysis/mapper/PromptAnalysisMapper.java`: `toEntity(request, userId)`, `toResponse(result, xpEarned, totalXp, tier, progress, earnedBadges)` static 메서드
- **Convention Notes**: Record 기반, `@Schema(description=...)` 필수, Mapper는 final class + private 생성자
- **Verification**: 빌드 성공
- **Exit Criteria**: DTO와 Mapper가 생성되고 빌드 통과
- **Status**: pending

### Todo 4: Service 구현
- **Priority**: 2
- **Dependencies**: Todo 1, Todo 2, Todo 3
- **Goal**: 분석 결과 저장 + XP 적립 + 배지 체크 비즈니스 로직을 구현한다
- **Work**:
  - `promptanalysis/service/PromptAnalysisService.java`:
    - `submitResult(UUID userId, PromptAnalysisResultRequest request)` 메서드
    - 로직:
      1. `PromptAnalysisMapper.toEntity()`로 엔티티 변환 후 저장
      2. `noImprovement=true` 또는 `cannotImprove=true`이면 xpEarned=0, 배지 체크 스킵
      3. 그렇지 않으면 `XP_PER_TOKEN * estimatedTokenSaving`으로 XP 계산
      4. `UserGameProfile` 조회/생성 후 XP 적립
      5. optimizeCount = `chatRepository.countOptimizedByUserId()` + `promptAnalysisResultRepository.countByUserId()` (합산)
      6. `badgeEventService.checkBadgesOnOptimize()` 호출
      7. tier/progress 계산
      8. Response 반환
    - 의존성: `PromptAnalysisResultRepository`, `UserGameProfileRepository`, `ChatRepository`, `BadgeEventService`
    - XP/티어 계산은 `UserGameService`의 `calculateTier`, `calculateProgress`를 참조하여 동일 로직 사용 (UserGameService에서 public으로 제공)
- **Convention Notes**: `@Service`, `@Transactional`, try-catch로 배지 체크 실패 시 무시 (기존 패턴)
- **Verification**: 빌드 성공
- **Exit Criteria**: 서비스 로직이 구현되고 빌드 통과
- **Status**: pending

### Todo 5: Controller 구현
- **Priority**: 3
- **Dependencies**: Todo 4
- **Goal**: API 엔드포인트를 구현한다
- **Work**:
  - `promptanalysis/controller/PromptAnalysisController.java`:
    - `@RestController`, `@RequestMapping("/api/v1/prompt-analysis")`
    - `@Tag(name = "프롬프트 분석", description = "AI 프롬프트 분석 결과 API")`
    - `POST /results` 메서드: `@Valid @RequestBody`, `@AuthenticationPrincipal UUID userId`
    - Swagger `@ApiResponses` 명세
- **Convention Notes**: 기존 `CrawlingController` 패턴 따름, `ResponseEntity` 반환
- **Verification**: 빌드 성공 + Swagger UI에서 API 확인
- **Exit Criteria**: 엔드포인트가 생성되고 빌드 통과
- **Status**: pending

### Todo 6: spotless 적용 및 최종 검증
- **Priority**: 4
- **Dependencies**: Todo 5
- **Goal**: 코드 포맷팅 및 최종 빌드 검증
- **Work**:
  - `./gradlew spotlessApply` 실행
  - `./gradlew build` 실행하여 전체 빌드 검증
- **Convention Notes**: spotless 포맷 필수 적용
- **Verification**: `./gradlew build` 성공
- **Exit Criteria**: spotless + 빌드 모두 통과
- **Status**: pending

## Verification Strategy
- `./gradlew spotlessApply` → 코드 포맷 검증
- `./gradlew build` → 컴파일 + 테스트 통과
- Swagger UI에서 `POST /api/v1/prompt-analysis/results` 엔드포인트 확인

## Progress Tracking
- Total Todos: 6
- Completed: 0
- Status: Planning complete

## Change Log
- 2026-02-22: Plan created
