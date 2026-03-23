package com.netflix.backend.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.netflix.backend.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final String SECRET = "my-secret-key-my-secret-key-my-secret-key";

	private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

	public String generateAccessToken(String email, int tokenVersion) {
		return Jwts.builder().subject(email).claim("version", tokenVersion).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)).signWith(key).compact();
	}

	public int extractVersion(String token) {
		return (Integer) getClaims(token).get("version");
	}

	private Claims getClaims(String token) {
		return safeGetClaims(token);
	}

	public Claims safeGetClaims(String token) {
		try {
			return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		} catch (Exception e) {
			throw new InvalidTokenException("Invalid JWT token");
		}
	}

	public String extractEmail(String token) {
		return getClaims(token).getSubject();
	}

	public boolean isValid(String token, String email) {
		return email.equals(extractEmail(token)) && !isExpired(token);
	}

	public boolean isExpired(String token) {
		return getClaims(token).getExpiration().before(new Date());
	}

	public String generateRefreshToken(String email) {
		return Jwts.builder().subject(email).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)).signWith(key).compact();
	}

	public long getRemainingValidity(String token) {
		Date expiration = getClaims(token).getExpiration();
		return expiration.getTime() - System.currentTimeMillis();
	}
}