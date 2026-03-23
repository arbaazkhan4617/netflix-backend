package com.netflix.backend.security.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.netflix.backend.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

	private final RedisTemplate<String, String> redisTemplate;
	private final JwtUtil jwtUtil;
	private static final String PREFIX = "blacklist:";
	private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

	public void blacklist(String token) {
		long expiry = jwtUtil.getRemainingValidity(token);
		String key = PREFIX + token;
		log.info("Blacklisting token with TTL: {}", expiry);
		redisTemplate.opsForValue().set(key, "1", expiry, TimeUnit.MILLISECONDS);
	}

	public boolean isBlacklisted(String token) {
		return redisTemplate.hasKey("blacklist:" + token);
	}
}