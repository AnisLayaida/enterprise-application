package com.example.project.btleavebookingsystem.leavemanagement.controller;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
class OutstandingRequestsFilterIntegrationTest {

    private static final String PASSWORD = "TestPass123!";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record LoginPayload(String username, String password) {}

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String createStaff(String adminToken, String username, String role, String lineManagerId) throws Exception {
        String lineManagerJson = lineManagerId == null ? "null" : "\"" + lineManagerId + "\"";
        String body = """
                {
                  "firstName": "Filter",
                  "surname": "Tester",
                  "email": "%s@bt.com",
                  "hireDate": "2023-04-01",
                  "department": "QA",
                  "lineManagerId": %s,
                  "jobLevel": "Engineer",
                  "employmentStatus": "ACTIVE",
                  "username": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(username, lineManagerJson, username, PASSWORD, role);

        MvcResult result = mockMvc.perform(post("/api/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void submitLeave(String token) throws Exception {
        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType": "ANNUAL", "startDate": "2026-11-02", "endDate": "2026-11-03",
                                 "reason": "Filter test", "requiresHRApproval": false}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void adminCanFilterOutstandingRequestsToASingleManagersTeam() throws Exception {
        String adminToken = login("admin", "AdminPass123!");

        String managerId = createStaff(adminToken, "filtermanager." + UUID.randomUUID(), "MANAGER", null);

        String teamMember = "teammember." + UUID.randomUUID();
        String teamMemberId = createStaff(adminToken, teamMember, "STAFF", managerId);

        String outsider = "outsider." + UUID.randomUUID();
        createStaff(adminToken, outsider, "STAFF", null);

        submitLeave(login(teamMember, PASSWORD));
        submitLeave(login(outsider, PASSWORD));

        mockMvc.perform(get("/api/leave-requests")
                        .param("managerId", managerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].staffId").value(teamMemberId));
    }
}