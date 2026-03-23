package com.netflix.backend.auth.dto;

public record RefreshRequest(String email, String refreshToken) {
}