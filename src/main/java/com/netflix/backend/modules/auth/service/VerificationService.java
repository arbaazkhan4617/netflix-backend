package com.netflix.backend.modules.auth.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.netflix.backend.common.email.EmailService;
import com.netflix.backend.entity.VerificationToken;
import com.netflix.backend.modules.user.repository.VerificationTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {

	private final VerificationTokenRepository repository;
	private final EmailService emailService;

	public void generateOtp(String email) {

		String otp = String.valueOf(new Random().nextInt(900000) + 100000);

		repository.deleteByEmail(email);

		repository.save(VerificationToken.builder().email(email).otp(otp).expiryDate(LocalDateTime.now().plusMinutes(5))
				.build());

		System.out.println("OTP for " + email + " is: " + otp);

		emailService.sendOtp(email, otp);

	}

	public void verifyOtp(String email, String otp) {

		VerificationToken token = repository.findByEmailAndOtp(email, otp)
				.orElseThrow(() -> new RuntimeException("Invalid OTP"));

		if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("OTP expired");
		}

		repository.delete(token);
	}
}