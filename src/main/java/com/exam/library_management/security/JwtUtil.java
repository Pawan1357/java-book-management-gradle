package com.exam.library_management.security;

import com.exam.library_management.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // 🔐 Secret key (DO NOT hardcode in real prod apps)
    // private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final SecretKey secretKey;
    private final long jwtExpirationMs;

    // public JwtUtil(
    //         @Value("${jwt.secret}") String secret,
    //         @Value("${jwt.expiration}") long jwtExpirationMs
    // ) {
    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(
                // secret.getBytes(StandardCharsets.UTF_8)
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
        this.jwtExpirationMs = jwtProperties.getExpiration();
    }

    // private static final long JWT_EXPIRATION_MS = 24 * 60 * 60 * 1000; // 1 day

    /* ===============================
       TOKEN GENERATION
       =============================== */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /* ===============================
       EXTRACT USERNAME
       =============================== */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /* ===============================
       VALIDATE TOKEN
       =============================== */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    /* ===============================
       HELPERS
       =============================== */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }
}
