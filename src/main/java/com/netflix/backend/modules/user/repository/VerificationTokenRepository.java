package com.netflix.backend.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.netflix.backend.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	Optional<VerificationToken> findByEmailAndOtp(String email, String otp);

	void deleteByEmail(String email);
}