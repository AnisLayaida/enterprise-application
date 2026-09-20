package com.example.project.btleavebookingsystem.leavemanagement.controller;

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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
class LeaveUsageReportIntegrationTest {

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

    private String createStaff(String adminToken, String username, String role) throws Exception {
        String body = """
                {
                  "firstName": "Usage",
                  "surname": "Tester",
                  "email": "%s@bt.com",
                  "hireDate": "2023-04-01",
                  "department": "QA",
                  "lineManagerId": null,
                  "jobLevel": "Engineer",
                  "employmentStatus": "ACTIVE",
                  "username": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(username, username, PASSWORD, role);

        MvcResult result = mockMvc.perform(post("/api/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void adminUsageReportReflectsApprovedAnnualLeave() throws Exception {
        String adminToken = login("admin", "AdminPass123!");
        String username = "usage." + UUID.randomUUID();
        String staffId = createStaff(adminToken, username, "STAFF");

        MvcResult submitted = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + login(username, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType": "ANNUAL", "startDate": "2026-11-02", "endDate": "2026-11-04",
                                 "reason": "Usage test", "requiresHRApproval": false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String requestId = objectMapper.readTree(submitted.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/leave-requests/" + requestId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\": true}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leave-allowance")
                        .param("businessYear", "2026")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessYear").value(2026))
                .andExpect(jsonPath("$.staff[?(@.staffId == '" + staffId + "')].usedDays", hasItem(3)))
                .andExpect(jsonPath("$.staff[?(@.staffId == '" + staffId + "')].remainingDays", hasItem(22)));
    }

    @Test
    void managerCannotViewTheSystemWideUsageReport() throws Exception {
        String adminToken = login("admin", "AdminPass123!");
        String managerUsername = "usagemanager." + UUID.randomUUID();
        createStaff(adminToken, managerUsername, "MANAGER");

        mockMvc.perform(get("/api/leave-allowance")
                        .header("Authorization", "Bearer " + login(managerUsername, PASSWORD)))
                .andExpect(status().isForbidden());
    }
}