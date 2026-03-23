package com.netflix.backend.auth.service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflix.backend.auth.dto.AuthRequest;
import com.netflix.backend.auth.dto.AuthResponse;
import com.netflix.backend.auth.dto.MessageResponse;
import com.netflix.backend.auth.dto.RefreshRequest;
import com.netflix.backend.auth.dto.VerifyRequest;
import com.netflix.backend.auth.util.RequestUtils;
import com.netflix.backend.common.email.EmailService;
import com.netflix.backend.entity.RefreshToken;
import com.netflix.backend.entity.Role;
import com.netflix.backend.entity.User;
import com.netflix.backend.exception.AccountNotVerifiedException;
import com.netflix.backend.exception.InvalidCredentialsException;
import com.netflix.backend.exception.TokenExpiredException;
import com.netflix.backend.exception.UserAlreadyExistsException;
import com.netflix.backend.exception.UserNotFoundException;
import com.netflix.backend.security.jwt.JwtUtil;
import com.netflix.backend.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	private final VerificationService verificationService;
	private final SessionService sessionService;
	private final RedisTemplate<String, String> redisTemplate;
	private final EmailService emailService;
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	@Transactional
	public MessageResponse signup(AuthRequest request) {

		if (userRepository.findByEmail(request.email()).isPresent()) {
			throw new UserAlreadyExistsException("User already exists");
		}

		User user = User.builder().email(request.email()).password(passwordEncoder.encode(request.password()))
				.role(Role.ROLE_USER).tokenVersion(0).isVerified(false).build();

		userRepository.save(user);

		verificationService.generateOtp(user.getEmail());

		return new MessageResponse("OTP sent to email");
	}

	@Transactional
	public MessageResponse verifyAccount(VerifyRequest request) {

		verificationService.verifyOtp(request.email(), request.otp());

		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setVerified(true);

		userRepository.save(user);

		return new MessageResponse("Account verified successfully");
	}

	@Transactional
	public AuthResponse login(AuthRequest request, HttpServletRequest httpRequest) {

		checkLoginAttempts(request.email());

		String ip = RequestUtils.getClientIp(httpRequest);
		String device = RequestUtils.getDevice(httpRequest);

		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		if (!user.isVerified()) {
			throw new AccountNotVerifiedException("Account not verified");
		}

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid credentials");
		}

		String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getTokenVersion());

		String rawToken = UUID.randomUUID().toString();

		String deviceId = UUID.randomUUID().toString();

		sessionService.createSession(user.getEmail(), deviceId, accessToken, ip, device);

		refreshTokenService.save(rawToken, user.getEmail(), "device");

		redisTemplate.delete("login:" + request.email());

		boolean suspicious = sessionService.isSuspicious(user.getEmail(), ip, device);

		if (suspicious) {
			log.warn("⚠ Suspicious login detected for {}", user.getEmail());
			emailService.sendAlert(user.getEmail(), ip, device);
		}
		user.setLastLoginAt(LocalDateTime.now());
		userRepository.save(user);
		return new AuthResponse(accessToken, rawToken, deviceId);
	}

	@Transactional
	public AuthResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {

		RefreshToken matched = refreshTokenService.validate(request.refreshToken(), request.email());

		if (matched.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException("Token expired");
		}

		String ip = RequestUtils.getClientIp(httpRequest);
		String device = RequestUtils.getDevice(httpRequest);

		String email = matched.getEmail();

		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

		// ROTATION
		refreshTokenService.delete(matched);

		String newAccess = jwtUtil.generateAccessToken(email, user.getTokenVersion());

		String newRawToken = UUID.randomUUID().toString();

		String deviceId = UUID.randomUUID().toString();

		sessionService.createSession(user.getEmail(), deviceId, newAccess, ip, device);

		refreshTokenService.save(newRawToken, email, "device");

		return new AuthResponse(newAccess, newRawToken, deviceId);
	}

	@Transactional
	public void logoutAllDevices(String email) {

		// 1. Invalidate all JWTs
		User user = userRepository.findByEmail(email).orElseThrow();
		user.setTokenVersion(user.getTokenVersion() + 1);
		userRepository.save(user);

		// 2. Delete refresh tokens from DB
		refreshTokenService.deleteAll(email);

		// 3. DELETE REDIS DEVICES + SESSIONS
		String deviceKey = "user:" + email + ":devices";

		Set<String> deviceIds = redisTemplate.opsForSet().members(deviceKey);

		if (deviceIds != null) {
			for (String deviceId : deviceIds) {
				redisTemplate.delete("device:" + deviceId);
				redisTemplate.delete("session:" + deviceId); // if used
			}
		}

		// 4. Remove device list
		redisTemplate.delete(deviceKey);
	}

	private void checkLoginAttempts(String email) {

		String key = "login:" + email;

		Long attempts = redisTemplate.opsForValue().increment(key);

		if (attempts == 1) {
			redisTemplate.expire(key, 1, TimeUnit.MINUTES);
		}

		if (attempts > 5) {
			throw new InvalidCredentialsException("Too many login attempts. Try again later.");
		}
	}
}