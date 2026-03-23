package com.netflix.backend.common.email;

public interface EmailService {

	void sendOtp(String to, Integer otp);

	void sendAlert(String email, String ip, String device);
}