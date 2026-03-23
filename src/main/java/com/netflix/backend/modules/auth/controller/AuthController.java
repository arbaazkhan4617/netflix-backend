package com.netflix.backend.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.backend.modules.auth.dto.AuthRequest;
import com.netflix.backend.modules.auth.dto.AuthResponse;
import com.netflix.backend.modules.auth.dto.MessageResponse;
import com.netflix.backend.modules.auth.dto.RefreshRequest;
import com.netflix.backend.modules.auth.dto.VerifyRequest;
import com.netflix.backend.modules.auth.service.AuthService;
import com.netflix.backend.security.jwt.JwtUtil;
import com.netflix.backend.security.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtUtil jwtUtil;
	private final TokenBlacklistService blacklistService;

	@PostMapping("/signup")
	public ResponseEntity<MessageResponse> signup(@Valid @RequestBody AuthRequest request) {
		return ResponseEntity.ok(authService.signup(request));
	}

	@PostMapping("/verify")
	public ResponseEntity<MessageResponse> verify(@RequestBody VerifyRequest request) {
		return ResponseEntity.ok(authService.verifyAccount(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
		return ResponseEntity.ok(authService.refresh(request));
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {

		String header = request.getHeader("Authorization");

		if (header == null || !header.startsWith("Bearer ")) {
			return ResponseEntity.badRequest().body("Missing token");
		}

		String token = header.substring(7);

		blacklistService.blacklist(token);

		String email = jwtUtil.extractEmail(token);

		authService.logoutAllDevices(email);

		return ResponseEntity.ok("Logged out successfully");
	}
}