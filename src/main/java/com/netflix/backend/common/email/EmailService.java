package com.netflix.backend.common.email;

public interface EmailService {

	void sendOtp(String to, String otp);
}