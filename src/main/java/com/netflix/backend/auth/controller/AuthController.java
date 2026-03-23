package com.netflix.backend.auth.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.backend.auth.dto.AuthRequest;
import com.netflix.backend.auth.dto.AuthResponse;
import com.netflix.backend.auth.dto.MessageResponse;
import com.netflix.backend.auth.dto.RefreshRequest;
import com.netflix.backend.auth.dto.SessionResponse;
import com.netflix.backend.auth.dto.VerifyRequest;
import com.netflix.backend.auth.service.AuthService;
import com.netflix.backend.auth.service.SessionService;
import com.netflix.backend.auth.service.UserService;
import com.netflix.backend.entity.User;
import com.netflix.backend.security.jwt.JwtUtil;
import com.netflix.backend.security.service.TokenBlacklistService;
import com.netflix.backend.user.repository.UserRepository;

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
	private final SessionService sessionService;
	private final UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<MessageResponse> signup(@Valid @RequestBody AuthRequest request) {
		return ResponseEntity.ok(authService.signup(request));
	}

	@PostMapping("/verify")
	public ResponseEntity<MessageResponse> verify(@RequestBody VerifyRequest request) {
		return ResponseEntity.ok(authService.verifyAccount(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
		return ResponseEntity.ok(authService.login(request, httpRequest));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
		return ResponseEntity.ok(authService.refresh(request, httpRequest));
	}

	@PostMapping("/logout-device")
	public ResponseEntity<String> logoutDevice(@RequestParam String deviceId) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Map<Object, Object> session = sessionService.getSession(deviceId);

		if (session == null || session.isEmpty()) {
			return ResponseEntity.badRequest().body("Session not found");
		}
		String token = (String) session.get("token");
		blacklistService.blacklist(token);
		sessionService.deleteSession(email, deviceId);
		return ResponseEntity.ok("Logged out from device");
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

	@GetMapping("/sessions")
	public ResponseEntity<List<SessionResponse>> getSessions(HttpServletRequest request) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		Set<String> devices = sessionService.getUserDevices(email);

		User user = userService.findByEmail(email);

		String lastLogin = user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null;

		String header = request.getHeader("Authorization");
		String currentToken = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;

		List<SessionResponse> sessions = devices.stream().map(deviceId -> {

			Map<Object, Object> session = sessionService.getSession(deviceId);

			if (session == null || session.isEmpty()) {
				return null;
			}

			String sessionToken = (String) session.get("token");

			boolean isCurrent = currentToken != null && currentToken.equals(sessionToken);

			return new SessionResponse(deviceId, (String) session.get("ip"), (String) session.get("device"),
					Long.parseLong((String) session.get("loginTime")), isCurrent, lastLogin);
		}).filter(Objects::nonNull).toList();

		return ResponseEntity.ok(sessions);
	}
}