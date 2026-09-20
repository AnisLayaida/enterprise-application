package com.example.project.btleavebookingsystem.identityaccess.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest(properties = {
        "rate-limit.login.max-requests=5",
        "rate-limit.api.max-requests=3"
})
@AutoConfigureMockMvc
class RateLimitingFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exceedingTheLoginLimitReturns429WithJsonBodyAndRetryAfterHeader() throws Exception {
        String body = """
                {"username": "does-not-exist", "password": "irrelevant"}
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(429))
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }

    @Test
    void theGeneralApiTierIsLimitedIndependentlyOfTheLoginTier() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/leave-requests/mine"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(get("/api/leave-requests/mine"))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.message").exists());
    }
}