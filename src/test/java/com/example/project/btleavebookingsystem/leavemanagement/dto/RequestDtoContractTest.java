package com.example.project.btleavebookingsystem.leavemanagement.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestDtoContractTest {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build();

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private static <T> Set<String> invalidFields(T dto) {
        return validator.validate(dto).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Submit: an absent HR flag is parsed and defaults to false")
    void submitAbsentHrFlagDefaultsToFalse() throws Exception {
        SubmitLeaveRequestDto dto = MAPPER.readValue("""
                {"leaveType": "ANNUAL", "startDate": "2026-12-07", "endDate": "2026-12-09"}
                """, SubmitLeaveRequestDto.class);

        assertFalse(dto.requiresHRApproval());
        assertTrue(invalidFields(dto).isEmpty());
    }

    @Test
    @DisplayName("Submit: missing required fields reach bean validation instead of failing in the parser")
    void submitMissingFieldsAreReportedByBeanValidation() throws Exception {
        SubmitLeaveRequestDto dto = MAPPER.readValue("""
                {"startDate": "2026-12-21", "endDate": "2026-12-22"}
                """, SubmitLeaveRequestDto.class);

        assertEquals(Set.of("leaveType"), invalidFields(dto));
    }

    @Test
    @DisplayName("Submit: an explicit HR flag is preserved")
    void submitExplicitHrFlagIsPreserved() throws Exception {
        SubmitLeaveRequestDto dto = MAPPER.readValue("""
                {"leaveType": "ANNUAL", "startDate": "2026-12-07", "endDate": "2026-12-09",
                 "requiresHRApproval": true}
                """, SubmitLeaveRequestDto.class);

        assertTrue(dto.requiresHRApproval());
    }

    @Test
    @DisplayName("Review: a missing decision is a validation error, never a silent rejection")
    void reviewMissingDecisionFailsValidation() throws Exception {
        ReviewLeaveRequestDto dto = MAPPER.readValue("{}", ReviewLeaveRequestDto.class);

        assertEquals(Set.of("approved"), invalidFields(dto));
    }

    @Test
    @DisplayName("Review: an explicit decision is preserved")
    void reviewExplicitDecisionIsPreserved() throws Exception {
        ReviewLeaveRequestDto dto = MAPPER.readValue("""
                {"approved": false}
                """, ReviewLeaveRequestDto.class);

        assertFalse(dto.approved());
        assertTrue(invalidFields(dto).isEmpty());
    }
}