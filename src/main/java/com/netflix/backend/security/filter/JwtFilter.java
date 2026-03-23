package com.netflix.backend.security.filter;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.netflix.backend.entity.User;
import com.netflix.backend.exception.InvalidTokenException;
import com.netflix.backend.exception.TokenExpiredException;
import com.netflix.backend.exception.UserNotFoundException;
import com.netflix.backend.modules.user.repository.UserRepository;
import com.netflix.backend.security.jwt.JwtUtil;
import com.netflix.backend.security.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final TokenBlacklistService blacklistService;

	private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
	private static final String AUTH_HEADER = "Authorization";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(AUTH_HEADER);

		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);

		try {

			if (blacklistService.isBlacklisted(token)) {
				throw new InvalidTokenException("Token is blacklisted");
			}

			if (jwtUtil.isExpired(token)) {
				throw new TokenExpiredException("JWT expired");
			}

			String email = jwtUtil.extractEmail(token);
			Integer tokenVersion = jwtUtil.extractVersion(token);

			if (tokenVersion.equals(null)) {
				throw new InvalidTokenException("Missing token version");
			}

			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UserNotFoundException("User not found"));

			if (tokenVersion != user.getTokenVersion()) {
				throw new TokenExpiredException("Token invalid (version mismatch)");
			}

			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getEmail(),
						null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));

				SecurityContextHolder.getContext().setAuthentication(auth);
			}

		} catch (Exception ex) {
			log.error("JWT error: {}", ex.getMessage());
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"Unauthorized\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}
}