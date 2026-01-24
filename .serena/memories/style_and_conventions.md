# Style and Conventions

## API Versioning
- Use `version` attribute in `@RequestMapping` (e.g., `@RequestMapping(version = "0.0.1", path = "/...")`).
- Format: `MAJOR.MINOR.PATCH`.

## Common Response
- All API responses are automatically wrapped in `ApiResponse<T>` by `ApiResponseWrapper`.
- Use `@SkipApiResponseWrapper` on controller methods or classes to bypass automatic wrapping.
- Errors should be handled via `ApiException` which returns `ApiExceptionResponse`.

## Status Codes
- Implement `StatusInterface` for custom status codes.
- Follow `customStatusCode` ranges defined in `README.md`.
    - 0: Success
    - 1~9: Default client error
    - 10~19: User domain
    - 20~29: Order domain
    - 90~99: Server error
