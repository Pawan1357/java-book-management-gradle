package com.exam.library_management.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ValidationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnBadRequestWhenRegisterFieldsMissing() throws Exception {

        String invalidRequest = """
                {
                  "email": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBookFieldsMissing() throws Exception {

        String invalidBookRequest = """
                {
                  "title": "",
                  "author": "",
                  "isbn": ""
                }
                """;

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBookRequest))
                .andExpect(status().isUnauthorized());
    }
}
