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
class LeaveRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private record LoginPayload(String username, String password) {}

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginPayload(username, password));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private String createStaffAndReturnToken(String adminToken, String username) throws Exception {
        String createStaffBody = """
                {
                  "firstName": "Integration",
                  "surname": "Tester",
                  "email": "%s@bt.com",
                  "hireDate": "2023-04-01",
                  "department": "QA",
                  "lineManagerId": null,
                  "jobLevel": "Engineer",
                  "employmentStatus": "ACTIVE",
                  "username": "%s",
                  "password": "TestPass123!",
                  "role": "STAFF"
                }
                """.formatted(username, username);

        mockMvc.perform(post("/api/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createStaffBody))
                .andExpect(status().isCreated());

        return loginAndGetToken(username, "TestPass123!");
    }

    @Test
    void staffCannotCreateAnotherStaffMember() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String staffToken = createStaffAndReturnToken(adminToken, "integration.staff." + UUID.randomUUID());

        String secondStaffBody = """
                {
                  "firstName": "Second",
                  "surname": "Attempt",
                  "email": "second@bt.com",
                  "hireDate": "2023-04-01",
                  "department": "QA",
                  "lineManagerId": null,
                  "jobLevel": "Engineer",
                  "employmentStatus": "ACTIVE",
                  "username": "second.%s",
                  "password": "TestPass123!",
                  "role": "STAFF"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/staff")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondStaffBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void staffCanSubmitTheirOwnLeaveRequestAndReceivesPendingStatus() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String staffToken = createStaffAndReturnToken(adminToken, "leave.tester." + UUID.randomUUID());

        String submitBody = """
                {
                  "leaveType": "ANNUAL",
                  "startDate": "2026-11-02",
                  "endDate": "2026-11-04",
                  "reason": "Integration test leave",
                  "requiresHRApproval": false
                }
                """;

        mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void adminCanFetchASingleLeaveRequestByIdAfterItIsSubmitted() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String staffToken = createStaffAndReturnToken(adminToken, "getbyid.tester." + UUID.randomUUID());

        String submitBody = """
                {
                  "leaveType": "ANNUAL",
                  "startDate": "2026-12-01",
                  "endDate": "2026-12-03",
                  "reason": "GetById test",
                  "requiresHRApproval": false
                }
                """;

        MvcResult submitResult = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andReturn();

        String requestId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/api/leave-requests/" + requestId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void staffCannotFetchALeaveRequestByIdDirectlyDueToRoleRestriction() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String staffTokenA = createStaffAndReturnToken(adminToken, "staffa." + UUID.randomUUID());

        String submitBody = """
                {
                  "leaveType": "ANNUAL",
                  "startDate": "2026-12-10",
                  "endDate": "2026-12-11",
                  "reason": "Restricted lookup test",
                  "requiresHRApproval": false
                }
                """;

        MvcResult submitResult = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + staffTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andReturn();

        String requestId = objectMapper.readTree(submitResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/api/leave-requests/" + requestId)
                        .header("Authorization", "Bearer " + staffTokenA))
                .andExpect(status().isForbidden());
    }
}