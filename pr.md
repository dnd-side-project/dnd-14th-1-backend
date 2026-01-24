# feat: 공통 응답 시스템 구현 및 Swagger 스키마 등록

## 개요
프로젝트 전반에서 사용할 공통 응답(Common Response) 시스템을 구축하고, 이를 Swagger UI와 연동하여 API 문서의 일관성을 확보했습니다. 또한 API 버저닝 전략 및 관련 문서화를 완료했습니다.

## 변경 사항

### 1. 공통 응답 시스템 구현 (`com.rokyai.dnd14th1backend.common`)
- **`ApiResponse<T>`**: 모든 성공 응답을 래핑하는 공통 DTO.
- **`ApiExceptionResponse<T>`**: 예외 발생 시 반환되는 에러 응답 DTO (커스텀 상태 코드 포함).
- **`ApiResponseWrapper`**: `ResponseBodyAdvice`를 구현하여 컨트롤러의 반환값을 자동으로 `ApiResponse`로 래핑.
- **`ApiExceptionHandler`**: `@RestControllerAdvice`를 통해 전역 예외 처리 및 `ApiException`을 통한 일관된 에러 응답 반환.
- **`StatusInterface` & `DefaultStatus`**: 도메인별 확장 가능한 상태 코드 체계 구축.

### 2. Swagger 설정 (`SwaggerConfig.java`)
- `OpenApiCustomizer`를 통해 `ApiResponse` 및 `ApiExceptionResponse` 스키마를 OpenAPI Components에 명시적으로 등록.
- `OperationCustomizer`를 통해 Swagger 문서상의 모든 API 응답이 `ApiResponse`로 감싸진 형태로 표시되도록 자동화.

### 3. 문서화 (`README.md`)
- 공통 응답 시스템의 위치, 구성 요소, 사용법(자동 래핑, 예외 발생, 커스텀 상태 코드 규칙)을 상세히 기술.
- API 버저닝 규칙(`MAJOR.MINOR.PATCH`) 및 적용 가이드 추가.
- Spotless 포맷팅 관련 명령어 안내 추가.

### 4. 환경 설정 (`.gitignore`)
- `.claude`, `.gemini` 등 AI 도구 관련 내부 디렉토리를 제외하여 개발 환경의 일관성 유지.

## 테스트 계획
- [x] `./gradlew classes`: 컴파일 성공 확인
- [x] `./gradlew test`: 전체 테스트 케이스 통과 확인
- [x] `./gradlew spotlessCheck`: 코드 스타일 검사 통과 확인
- [x] Swagger UI 시각적 확인: `http://localhost:8080/swagger-ui/index.html` 접속 시 모든 API 응답이 `ApiResponse`로 래핑되어 표시되며, Schemas 섹션에 관련 객체들이 정상 노출됨을 확인.