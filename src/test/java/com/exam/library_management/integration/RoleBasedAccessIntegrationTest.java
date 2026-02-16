package com.exam.library_management.integration;

import com.exam.library_management.entity.User;
import com.exam.library_management.enums.Role;
import com.exam.library_management.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoleBasedAccessIntegrationTest extends BaseIntegrationTest {

    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String login(String username) throws Exception {
        String loginPayload = String.format("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """, username, PASSWORD);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        String token = node.get("data").get("token").asText();
        assertNotNull(token);
        return token;
    }

    private String createAdminAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "admin-role-" + unique + "@test.com";

        User admin = new User();
        admin.setLibraryId("ADMIN" + unique);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(PASSWORD));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        return login(email);
    }

    private String registerUserAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "user-role-" + unique + "@test.com";
        String libraryId = "USER" + unique;

        String registerPayload = String.format("""
                {
                  "libraryId": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """, libraryId, email, PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk());

        return login(email);
    }

    @Test
    void shouldAllowAdminToAccessAdminEndpoints() throws Exception {
        String adminToken = createAdminAndLogin();

        String bookRequest = """
                {
                  "bookCode": "ROLE-1001",
                  "title": "Admin Book",
                  "author": "Role Author"
                }
                """;

        mockMvc.perform(post("/api/admin/books/add")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/admin/books")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/admin/books/delete/{id}", 1L)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldDenyUserAccessingAdminEndpoints() throws Exception {
        String userToken = registerUserAndLogin();

        String bookRequest = """
                {
                  "bookCode": "ROLE-2001",
                  "title": "User Book",
                  "author": "Role Author"
                }
                """;

        mockMvc.perform(post("/api/admin/books/add")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookRequest))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/books")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
