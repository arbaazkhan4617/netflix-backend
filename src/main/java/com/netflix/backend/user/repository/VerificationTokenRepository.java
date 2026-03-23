package com.netflix.backend.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.netflix.backend.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	Optional<VerificationToken> findByEmailAndOtp(String email, Integer otp);

	void deleteByEmail(String email);
}