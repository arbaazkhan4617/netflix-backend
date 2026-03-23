package com.netflix.backend.modules.auth.dto;

public record RefreshRequest(String email, String refreshToken) {
}