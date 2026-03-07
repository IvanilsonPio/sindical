package com.sindicato.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtUtil, "secret", 
            "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 86400000L); // 24 hours

        userDetails = new User(
            "testuser",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtUtil.generateToken(userDetails);
        
        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        Boolean isValid = jwtUtil.validateToken(invalidToken);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldExtractExpirationDate() {
        String token = jwtUtil.generateToken(userDetails);
        Date expiration = jwtUtil.extractExpiration(token);
        
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void shouldGenerateRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }

    @Test
    void shouldValidateRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        Boolean isValid = jwtUtil.validateToken(refreshToken);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldGenerateDifferentTokensForAccessAndRefresh() {
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        assertThat(accessToken).isNotEqualTo(refreshToken);
    }
}
