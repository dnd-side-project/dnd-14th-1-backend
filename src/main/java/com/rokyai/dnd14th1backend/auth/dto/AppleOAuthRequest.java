package com.rokyai.dnd14th1backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.rokyai.dnd14th1backend.auth.enums.Platform;

/** Apple OAuth 로그인 요청. */
@Schema(description = "Apple OAuth 로그인 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppleOAuthRequest {

    @Schema(
            description = "디바이스 ID",
            example = "012Ab20E1C584BE285B42955491C35B",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "deviceId는 필수입니다")
    private String deviceId;

    @Schema(description = "플랫폼", example = "IOS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "platform은 필수입니다") private Platform platform;

    @Schema(
            description = "앱 패키지명",
            example = "com.photocard.master",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "package는 필수입니다")
    private String packageName;

    @Schema(
            description = "Apple ID Token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "idToken은 필수입니다")
    private String idToken;
}
