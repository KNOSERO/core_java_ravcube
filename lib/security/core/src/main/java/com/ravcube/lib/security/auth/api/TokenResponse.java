package com.ravcube.lib.security.auth.api;

public record TokenResponse(String accessToken, String refreshToken) {
}
