package com.exam.library_management.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerPayload(String libraryId, String email, String password) {
        return String.format("""
                {
                  "libraryId": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """, libraryId, email, password);
    }

    private String loginPayload(String username, String password) {
        return String.format("""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """, username, password);
    }

    private MvcResult register(String libraryId, String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(libraryId, email, password)))
                .andReturn();
    }

    @Test
    void shouldRegisterAndLoginSuccessfullyUsingEmail() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("LIB1001", "integration@test.com", "Password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.libraryId").value("LIB1001"));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("integration@test.com", "Password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        String token = node.get("data").get("token").asText();

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldLoginSuccessfullyUsingLibraryId() throws Exception {

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("LIB1002", "library-login@test.com", "Password123")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("LIB1002", "Password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void shouldReturnBadRequestForDuplicateEmail() throws Exception {

        register("LIB1003", "duplicate-email@test.com", "Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("LIB1004", "duplicate-email@test.com", "Password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnBadRequestForDuplicateLibraryId() throws Exception {

        register("LIB1005", "first-user@test.com", "Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("LIB1005", "second-user@test.com", "Password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginCredentials() throws Exception {

        register("LIB1006", "invalid-login@test.com", "Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("invalid-login@test.com", "WrongPassword")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnBadRequestForInvalidRegisterPayload() throws Exception {

        String invalidRegisterRequest = """
                {
                  "libraryId": "",
                  "email": "not-an-email",
                  "password": ""
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRegisterRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(node.has("errors") || node.has("message"));
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginPayload() throws Exception {

        String invalidLoginRequest = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(node.has("errors") || node.has("message"));
    }
}
