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

import java.time.Year;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
@AutoConfigureMockMvc
class LeaveAllowanceControllerIntegrationTest {

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

    private JsonNode createStaff(String adminToken, String username) throws Exception {
        String createStaffBody = """
                {
                  "firstName": "Allowance",
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

        MvcResult result = mockMvc.perform(post("/api/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createStaffBody))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void adminCanViewAnyStaffMembersAllowanceByStaffId() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        JsonNode staff = createStaff(adminToken, "viewallowance." + UUID.randomUUID());
        String staffId = staff.get("id").asText();

        mockMvc.perform(get("/api/leave-allowance/" + staffId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entitledDays").value(25))
                .andExpect(jsonPath("$.remainingDays").value(25));
    }

    @Test
    void adminCanCreateAnAllowanceForANewBusinessYear() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        JsonNode staff = createStaff(adminToken, "createallowance." + UUID.randomUUID());
        String staffId = staff.get("id").asText();

        int nextYear = Year.now().getValue() + 1;
        String createBody = """
                {"staffId": "%s", "businessYear": %d, "entitledDays": 28}
                """.formatted(staffId, nextYear);

        mockMvc.perform(post("/api/leave-allowance")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.businessYear").value(nextYear))
                .andExpect(jsonPath("$.entitledDays").value(28));
    }

    @Test
    void creatingADuplicateAllowanceForTheSameStaffAndYearReturnsConflict() throws Exception {
        String adminToken = loginAndGetToken("admin", "AdminPass123!");
        JsonNode staff = createStaff(adminToken, "duplicateallowance." + UUID.randomUUID());
        String staffId = staff.get("id").asText();

        int currentYear = Year.now().getValue();
        String createBody = """
                {"staffId": "%s", "businessYear": %d, "entitledDays": 25}
                """.formatted(staffId, currentYear);

        mockMvc.perform(post("/api/leave-allowance")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isConflict());
    }
}