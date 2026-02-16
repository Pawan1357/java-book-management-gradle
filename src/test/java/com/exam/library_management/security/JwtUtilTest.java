package com.exam.library_management.security;

import com.exam.library_management.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtUtilTest {

    @Test
    void validateToken_shouldReturnFalse_forMalformedToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("abcdefghijklmnopqrstuvwxyz123456");
        properties.setExpiration(60_000L);

        JwtUtil jwtUtil = new JwtUtil(properties);
        UserDetails user = User.withUsername("user@test.com")
                .password("ignored")
                .roles("USER")
                .build();

        assertFalse(jwtUtil.validateToken("not-a-jwt", user));
    }
}
