# Auth Exception Conventions - Fixed

## 1. Exception Hierarchy (ApiException based)
- AuthException → extends ApiException (requires StatusInterface)
- InvalidIdTokenException → extends ApiException

## 2. Status Codes (1000-unit system)
Auth domain: 1000~1999

| Status | HTTP | Code | Description |
|--------|------|------|-------------|
| INVALID_ID_TOKEN | 400 | 1400 | Bad Request - malformed token |
| INVALID_ISSUER | 401 | 1401 | Unauthorized - wrong issuer |
| INVALID_AUDIENCE | 403 | 1403 | Forbidden - wrong audience |
| INVALID_REFRESH_TOKEN | 401 | 1401 | Unauthorized - invalid token |
| EXPIRED_TOKEN | 401 | 1401 | Unauthorized - expired |

## 3. AppleIdTokenValidator Fixes
✅ Method returns DecodedJWT (not void)
✅ Uses specific AuthStatus for different validation failures
✅ Removed unused appleTeamId field
✅ All exceptions include StatusInterface parameter

## 4. Convention Compliance
- All exceptions extend ApiException
- All exceptions use StatusInterface (AuthStatus)
- HTTP status codes semantic (400=bad request, 401=unauthorized, 403=forbidden)
- Custom codes map to HTTP status meaning
