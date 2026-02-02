package com.rokyai.dnd14th1backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Apple ID Token 페이로드. Apple ID Token을 디코딩한 후 검증된 클레임들을 담습니다. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppleIdTokenPayload {

    @JsonProperty("sub")
    private String subject;

    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("aud")
    private String audience;

    @JsonProperty("iss")
    private String issuer;

    @JsonProperty("iat")
    private Long issuedAt;

    @JsonProperty("exp")
    private Long expiresAt;

    @JsonProperty("c_hash")
    private String codeHash;

    @JsonProperty("auth_time")
    private Long authTime;

    @JsonProperty("nonce_supported")
    private Boolean nonceSupported;
}
