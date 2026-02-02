# Style and Conventions

## API Versioning
- Use `version` attribute in `@RequestMapping` (e.g., `@RequestMapping(version = "0.0.1", path = "/...")`).
- Format: `MAJOR.MINOR.PATCH`.

## Common Response
- All API responses are automatically wrapped in `ApiResponse<T>` by `ApiResponseWrapper`.
- Use `@SkipApiResponseWrapper` on controller methods or classes to bypass automatic wrapping.
- Errors should be handled via `ApiException` which returns `ApiExceptionResponse`.

## Status Codes (1000-unit system)
- Implement `StatusInterface` for custom status codes.
- Follow `customStatusCode` ranges defined in `README.md`.
    - 0: Success
    - 1~9: Default client error
    - 1000~1999: Auth domain
    - 2000~2999: Order domain
    - 3000~3999: User domain
    - 9000~9999: Server error
