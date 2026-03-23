package com.netflix.backend.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netflix.backend.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

	private String secret;
	private Long accessExpiry;
	private Long refreshExpiry;
	private SecretKey key;
	
	@PostConstruct
	public void init() {
	    this.key = Keys.hmacShaKeyFor(secret.getBytes());
	}
	
	public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.access.expiry}") Long accessExpiry, @Value("${jwt.refresh.expiry}") Long refreshExpiry) {
		this.secret = secret;
		this.accessExpiry = accessExpiry;
		this.refreshExpiry = refreshExpiry;
	}	

	public String generateAccessToken(String email, Integer tokenVersion) {
		return Jwts.builder().subject(email).claim("version", tokenVersion).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + accessExpiry)).signWith(key).compact();
	}

	public Integer extractVersion(String token) {
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
				.expiration(new Date(System.currentTimeMillis() + refreshExpiry)).signWith(key).compact();
	}

	public long getRemainingValidity(String token) {
		Date expiration = getClaims(token).getExpiration();
		return expiration.getTime() - System.currentTimeMillis();
	}
}