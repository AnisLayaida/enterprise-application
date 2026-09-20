package com.example.project.btleavebookingsystem.identityaccess.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
class UserRoleIntegrationTest {

    private static final String PASSWORD = "TestPass123!";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record LoginPayload(String username, String password) {}

    private MvcResult loginResult(String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(username, password))))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String token(String username, String password) throws Exception {
        return objectMapper.readTree(loginResult(username, password).getResponse().getContentAsString())
                .get("token").asText();
    }

    private String createStaff(String adminToken, String username) throws Exception {
        String body = """
                {
                  "firstName": "Role",
                  "surname": "Tester",
                  "email": "%s@bt.com",
                  "hireDate": "2023-04-01",
                  "department": "QA",
                  "lineManagerId": null,
                  "jobLevel": "Engineer",
                  "employmentStatus": "ACTIVE",
                  "username": "%s",
                  "password": "%s",
                  "role": "STAFF"
                }
                """.formatted(username, username, PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void adminPromotesStaffToManagerAndTheNewRoleAppliesFromTheNextLogin() throws Exception {
        String adminToken = token("admin", "AdminPass123!");
        String username = "promote." + UUID.randomUUID();
        String staffId = createStaff(adminToken, username);

        mockMvc.perform(patch("/api/users/" + staffId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"MANAGER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffId").value(staffId))
                .andExpect(jsonPath("$.role").value("MANAGER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(username, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void staffCannotChangeAnyonesRole() throws Exception {
        String adminToken = token("admin", "AdminPass123!");
        String username = "notadmin." + UUID.randomUUID();
        String staffId = createStaff(adminToken, username);

        mockMvc.perform(patch("/api/users/" + staffId + "/role")
                        .header("Authorization", "Bearer " + token(username, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }
}