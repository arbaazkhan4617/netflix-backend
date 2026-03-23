package com.netflix.backend.auth.dto;

public record SessionResponse(String deviceId, String ip, String device, long loginTime, boolean current,
		String lastLoginAt) {
}