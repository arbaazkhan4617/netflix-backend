package com.netflix.backend.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.netflix.backend.entity.RefreshToken;
import com.netflix.backend.user.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final PasswordEncoder passwordEncoder;

    public void save(String rawToken, String email, String device) {

        String hash = passwordEncoder.encode(rawToken);

        repository.save(RefreshToken.builder()
                .tokenHash(hash)
                .email(email)
                .device(device)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build());
    }

    public RefreshToken validate(String rawToken, String email) {

        Optional<RefreshToken> tokenOpt = repository.findByTokenHash(email);
        
        RefreshToken token = tokenOpt
                .orElseThrow(() -> new RuntimeException("No refresh token found"));

        if (!passwordEncoder.matches(rawToken, token.getTokenHash())) {
            throw new RuntimeException("Invalid refresh token");
        }

        return token;
    }

    public void delete(RefreshToken token) {
        repository.delete(token);
    }

    public void deleteAll(String email) {
        repository.deleteByEmail(email);
    }
}