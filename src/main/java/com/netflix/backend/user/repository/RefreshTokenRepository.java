package com.netflix.backend.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.netflix.backend.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String token);

	List<RefreshToken> findByEmail(String email);

	void deleteByEmail(String email);
}