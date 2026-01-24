# feat: ApiResponse Swagger 스키마 등록 및 문서화

## 개요
Swagger UI의 Components/Schemas 섹션에 `ApiResponse` 및 `ApiExceptionResponse`가 노출되지 않는 문제를 해결하고, 공통 응답 구조 및 API 버저닝 전략에 대한 프로젝트 문서를 최신화했습니다.

## 변경 사항

### 1. Swagger 설정 (`SwaggerConfig.java`)
- `createApiResponseSchema()` 및 `createApiExceptionResponseSchema()` 메서드를 추가하여 `ApiResponse`와 `ApiExceptionResponse` 클래스를 OpenAPI Components 스키마에 명시적으로 등록했습니다.
- 이제 Swagger UI에서 공통 응답 래퍼의 구조를 정확하게 확인할 수 있습니다.

### 2. 문서화 (`README.md`)
- **Common Response (공통 응답)**: `ApiResponse`, `ApiExceptionResponse`, `StatusInterface` 등 공통 응답 시스템의 구성 요소와 사용법을 상세히 기술했습니다.
- **API Versioning**: `@RequestMapping`의 `version` 속성을 활용한 API 버저닝 규칙과 예시를 추가했습니다.
- **Code Formatting**: Spotless 사용법을 문서화했습니다.

### 3. 기타 (`.gitignore`)
- `.claude`, `.gemini` 등 AI 도구 관련 디렉토리를 gitignore에 추가하여 불필요한 파일이 커밋되지 않도록 수정했습니다.

## 테스트 계획
- [x] `./gradlew classes` 빌드 성공 확인
- [x] `./gradlew test` 전체 테스트 통과 확인
- [x] `./gradlew spotlessCheck` 포맷팅 검사 통과 확인
- [ ] 애플리케이션 실행 후 `http://localhost:8080/swagger-ui/index.html` 접속 시 Schemas 섹션에 `ApiResponse`가 표시되는지 확인
