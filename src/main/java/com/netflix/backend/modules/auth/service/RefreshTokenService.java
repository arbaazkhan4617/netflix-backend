package com.netflix.backend.modules.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.netflix.backend.entity.RefreshToken;
import com.netflix.backend.modules.user.repository.RefreshTokenRepository;

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

        List<RefreshToken> tokens = repository.findByEmail(email);

        return tokens.stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    public void delete(RefreshToken token) {
        repository.delete(token);
    }

    public void deleteAll(String email) {
        repository.deleteByEmail(email);
    }
}