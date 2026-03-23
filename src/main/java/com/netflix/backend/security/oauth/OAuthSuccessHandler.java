package com.netflix.backend.security.oauth;

import com.netflix.backend.entity.User;
import com.netflix.backend.modules.auth.service.AuthService;
import com.netflix.backend.modules.auth.service.RefreshTokenService;
import com.netflix.backend.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        assert user != null;
        String email = user.getAttribute("email");
        String name = user.getAttribute("name");

        if (email == null) {
            throw new IllegalArgumentException("Email not found from Google OAuth");
        }


        System.out.println("Google User: " + email);

        User dbUser = authService.handleOAuthUser(email, name);

        String accessToken = jwtUtil.generateAccessToken(dbUser.getEmail(), dbUser.getTokenVersion());

        String rawToken = UUID.randomUUID().toString();

        refreshTokenService.save(rawToken, dbUser.getEmail(), "device");

        response.sendRedirect("/netflix-backend/oauth/success?accessToken=" + accessToken);

    }
}