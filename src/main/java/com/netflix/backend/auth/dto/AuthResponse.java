package com.netflix.backend.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, String deviceId) {
}