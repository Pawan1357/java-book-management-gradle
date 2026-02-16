package com.exam.library_management.integration;

import com.exam.library_management.config.JwtProperties;
import com.exam.library_management.entity.User;
import com.exam.library_management.enums.Role;
import com.exam.library_management.repository.UserRepository;
import com.exam.library_management.security.CustomUserDetailsService;
import com.exam.library_management.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityAndExceptionCoverageIntegrationTest.TestOnlyErrorController.class)
public class SecurityAndExceptionCoverageIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RestController
    @RequestMapping("/api/auth/test-errors")
    static class TestOnlyErrorController {

        @GetMapping("/duplicate")
        public void duplicate() {
            throw new DataIntegrityViolationException("duplicate");
        }

        @GetMapping("/generic")
        public void generic() throws Exception {
            throw new Exception("generic");
        }
    }

    private User createUser() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLibraryId("LIB" + unique);
        user.setEmail("sec-" + unique + "@test.com");
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String tokenWithSubject(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String tokenWithoutSubject() {
        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String expiredTokenWithSubject(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );

        Date issuedAt = new Date(System.currentTimeMillis() - 120_000);
        Date expiredAt = new Date(System.currentTimeMillis() - 60_000);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void shouldMapDataIntegrityViolationToDuplicateKeyHandler() throws Exception {
        mockMvc.perform(get("/api/auth/test-errors/duplicate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Book with this book code already exists"));
    }

    @Test
    void shouldMapCheckedExceptionToGenericHandler() throws Exception {
        mockMvc.perform(get("/api/auth/test-errors/generic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Something went wrong. Please try again."));
    }

    @Test
    void shouldThrowUsernameNotFoundWhenUserMissing() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing-user-id")
        );
    }

    @Test
    void shouldReturnFalseWhenJwtSubjectDoesNotMatchLoadedUserEmail() {
        User user = createUser();
        String token = tokenWithSubject(user.getLibraryId());
        UserDetails loaded = customUserDetailsService.loadUserByUsername(user.getLibraryId());

        boolean valid = jwtUtil.validateToken(token, loaded);

        assertFalse(valid);
    }

    @Test
    void shouldReturnTrueWhenJwtSubjectMatchesAndNotExpired() {
        User user = createUser();
        String token = tokenWithSubject(user.getEmail());
        UserDetails loaded = customUserDetailsService.loadUserByUsername(user.getEmail());

        boolean valid = jwtUtil.validateToken(token, loaded);

        assertTrue(valid);
    }

    @Test
    void shouldReturnFalseWhenJwtIsExpired() {
        User user = createUser();
        String token = expiredTokenWithSubject(user.getEmail());
        UserDetails loaded = customUserDetailsService.loadUserByUsername(user.getEmail());

        boolean valid = jwtUtil.validateToken(token, loaded);

        assertFalse(valid);
    }

    @Test
    void shouldIgnoreMalformedBearerTokenAndRemainUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Bearer malformed.token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldIgnoreNonBearerAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Basic abc.def")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenBearerTokenSubjectIsLibraryId() throws Exception {
        User user = createUser();
        String token = tokenWithSubject(user.getLibraryId());

        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenBearerTokenHasNoSubject() throws Exception {
        String token = tokenWithoutSubject();

        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "mock@test.com", roles = "USER")
    void shouldSkipJwtAuthWhenSecurityContextAlreadyHasAuthentication() throws Exception {
        User user = createUser();
        String token = tokenWithSubject(user.getEmail());

        mockMvc.perform(get("/api/user/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
