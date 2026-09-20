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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
class LeaveRequestControllerIntegrationTest {

    private static final String PASSWORD = "TestPass123!";

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

    private String createStaff(String adminToken, String username, String role, String lineManagerId) throws Exception {
        String lineManagerJson = lineManagerId == null ? "null" : "\"" + lineManagerId + "\"";
        String createStaffBody = """
                {
                  "firstName": "Integration",
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
                        .content(createStaffBody))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createStaffAndReturnToken(String adminToken, String username) throws Exception {
        createStaff(adminToken, username, "STAFF", null);
        return loginAndGetToken(username, PASSWORD);
    }

    private String submitLeave(String staffToken, String startDate, String endDate) throws Exception {
        String submitBody = """
                {
                  "leaveType": "ANNUAL",
                  "startDate": "%s",
                  "endDate": "%s",
                  "reason": "Ownership test",
                  "requiresHRApproval": false
                }
                """.formatted(startDate, endDate);

        MvcResult result = mockMvc.perform(post("/api/leave-requests")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
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

        String requestId = submitLeave(staffToken, "2026-12-01", "2026-12-03");

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

        String requestId = submitLeave(staffTokenA, "2026-12-10", "2026-12-11");

        mockMvc.perform(get("/api/leave-requests/" + requestId)
                        .header("Authorization", "Bearer " + staffTokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    void historyReplaysThePersistedEventStreamToTheSameStatusAsTheStateTable() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String staffToken = createStaffAndReturnToken(adminToken, "history.tester." + UUID.randomUUID());

        String requestId = submitLeave(staffToken, "2026-12-14", "2026-12-16");

        mockMvc.perform(patch("/api/leave-requests/" + requestId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\": true}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leave-requests/" + requestId + "/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(2))
                .andExpect(jsonPath("$.events[0].sequenceNumber").value(1))
                .andExpect(jsonPath("$.events[0].eventType").value("LeaveRequestSubmittedEvent"))
                .andExpect(jsonPath("$.events[1].eventType").value("LeaveRequestApprovedEvent"))
                .andExpect(jsonPath("$.events[1].payload.numberOfDays").value(3))
                .andExpect(jsonPath("$.currentStatus").value("APPROVED"))
                .andExpect(jsonPath("$.replayedStatus").value("APPROVED"))
                .andExpect(jsonPath("$.consistent").value(true));
    }

    @Test
    void staffCannotCancelAnotherStaffMembersLeaveRequest() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String ownerToken = createStaffAndReturnToken(adminToken, "owner." + UUID.randomUUID());
        String intruderToken = createStaffAndReturnToken(adminToken, "intruder." + UUID.randomUUID());

        String requestId = submitLeave(ownerToken, "2026-11-09", "2026-11-10");

        mockMvc.perform(patch("/api/leave-requests/" + requestId + "/cancel")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action"));
    }

    @Test
    void lineManagerCanReviewTheirDirectReportsLeaveRequest() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String managerUsername = "linemanager." + UUID.randomUUID();
        String managerId = createStaff(adminToken, managerUsername, "MANAGER", null);
        String reportUsername = "report." + UUID.randomUUID();
        createStaff(adminToken, reportUsername, "STAFF", managerId);

        String requestId = submitLeave(loginAndGetToken(reportUsername, PASSWORD), "2026-11-16", "2026-11-17");

        mockMvc.perform(patch("/api/leave-requests/" + requestId + "/review")
                        .header("Authorization", "Bearer " + loginAndGetToken(managerUsername, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void managerCannotReviewALeaveRequestFromOutsideTheirTeam() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        String managerUsername = "othermanager." + UUID.randomUUID();
        createStaff(adminToken, managerUsername, "MANAGER", null);
        String staffToken = createStaffAndReturnToken(adminToken, "unmanaged." + UUID.randomUUID());

        String requestId = submitLeave(staffToken, "2026-11-23", "2026-11-24");

        mockMvc.perform(patch("/api/leave-requests/" + requestId + "/review")
                        .header("Authorization", "Bearer " + loginAndGetToken(managerUsername, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\": true}"))
                .andExpect(status().isForbidden());
    }
}