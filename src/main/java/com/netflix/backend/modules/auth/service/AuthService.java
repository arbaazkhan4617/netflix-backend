package com.netflix.backend.modules.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.netflix.backend.entity.AuthProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflix.backend.entity.RefreshToken;
import com.netflix.backend.entity.Role;
import com.netflix.backend.entity.User;
import com.netflix.backend.exception.AccountNotVerifiedException;
import com.netflix.backend.exception.InvalidCredentialsException;
import com.netflix.backend.exception.TokenExpiredException;
import com.netflix.backend.exception.UserAlreadyExistsException;
import com.netflix.backend.exception.UserNotFoundException;
import com.netflix.backend.modules.auth.dto.AuthRequest;
import com.netflix.backend.modules.auth.dto.AuthResponse;
import com.netflix.backend.modules.auth.dto.MessageResponse;
import com.netflix.backend.modules.auth.dto.RefreshRequest;
import com.netflix.backend.modules.auth.dto.VerifyRequest;
import com.netflix.backend.modules.user.repository.UserRepository;
import com.netflix.backend.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	private final VerificationService verificationService;

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

	public User handleOAuthUser(String email, String name) {

		Optional<User> optionalUser = userRepository.findByEmail(email);

		if (optionalUser.isPresent()) {

			User existingUser = optionalUser.get();

			if (existingUser.getProvider() == AuthProvider.LOCAL) {

				existingUser.setProvider(AuthProvider.GOOGLE);
				existingUser.setVerified(true);

				return userRepository.save(existingUser);
			}

			return existingUser;
		}

		User newUser = User.builder()
				.email(email)
				.password(null)
				.provider(AuthProvider.GOOGLE)
				.role(Role.ROLE_USER)
				.tokenVersion(0)
				.isVerified(true)
				.build();

		return userRepository.save(newUser);
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

	public AuthResponse login(AuthRequest request) {

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

		refreshTokenService.save(rawToken, user.getEmail(), "device");

		return new AuthResponse(accessToken, rawToken);
	}

	public AuthResponse refresh(RefreshRequest request) {

		RefreshToken matched = refreshTokenService.validate(request.refreshToken(), request.email());

		if (matched.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException("Token expired");
		}

		String email = matched.getEmail();

		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

		// ROTATION
		refreshTokenService.delete(matched);

		String newAccess = jwtUtil.generateAccessToken(email, user.getTokenVersion());

		String newRawToken = UUID.randomUUID().toString();

		refreshTokenService.save(newRawToken, email, "device");

		return new AuthResponse(newAccess, newRawToken);
	}

	public void logoutAllDevices(String email) {
		User user = userRepository.findByEmail(email).orElseThrow();
		user.setTokenVersion(user.getTokenVersion() + 1);
		userRepository.save(user);

		refreshTokenService.deleteAll(email);
	}
}