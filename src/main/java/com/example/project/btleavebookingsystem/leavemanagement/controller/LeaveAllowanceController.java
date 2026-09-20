package com.example.project.btleavebookingsystem.leavemanagement.controller;

import com.example.project.btleavebookingsystem.identityaccess.security.AuthenticatedUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.dto.AmendLeaveAllowanceDto;
import com.example.project.btleavebookingsystem.leavemanagement.dto.CreateLeaveAllowanceDto;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveAllowanceResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveUsageReportDto;
import com.example.project.btleavebookingsystem.leavemanagement.service.command.AmendLeaveAllowanceService;
import com.example.project.btleavebookingsystem.leavemanagement.service.command.CreateLeaveAllowanceService;
import com.example.project.btleavebookingsystem.leavemanagement.service.query.GetLeaveUsageReportService;
import com.example.project.btleavebookingsystem.leavemanagement.service.query.GetMyLeaveBalanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leave-allowance")
public class LeaveAllowanceController {

    private final GetMyLeaveBalanceService getMyLeaveBalanceService;
    private final AmendLeaveAllowanceService amendLeaveAllowanceService;
    private final CreateLeaveAllowanceService createLeaveAllowanceService;
    private final GetLeaveUsageReportService getLeaveUsageReportService;

    public LeaveAllowanceController(GetMyLeaveBalanceService getMyLeaveBalanceService,
                                    AmendLeaveAllowanceService amendLeaveAllowanceService,
                                    CreateLeaveAllowanceService createLeaveAllowanceService,
                                    GetLeaveUsageReportService getLeaveUsageReportService) {
        this.getMyLeaveBalanceService = getMyLeaveBalanceService;
        this.amendLeaveAllowanceService = amendLeaveAllowanceService;
        this.createLeaveAllowanceService = createLeaveAllowanceService;
        this.getLeaveUsageReportService = getLeaveUsageReportService;
    }

    /**
     * Administrator usage report: company-wide by default, optionally for one business year
     * and/or one manager's team.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveUsageReportDto> getUsageReport(
            @RequestParam(required = false) Integer businessYear,
            @RequestParam(required = false) UUID managerId) {
        return ResponseEntity.ok(getLeaveUsageReportService.getReport(businessYear, managerId));
    }

    @GetMapping("/mine")
    public ResponseEntity<LeaveAllowanceResponseDto> getMine(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(getMyLeaveBalanceService.getForStaff(user.staffId()));
    }

    @GetMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveAllowanceResponseDto> getForStaff(@PathVariable UUID staffId,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(getMyLeaveBalanceService.getForStaff(staffId, ActingUser.from(authentication)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveAllowanceResponseDto> create(@Valid @RequestBody CreateLeaveAllowanceDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createLeaveAllowanceService.create(dto));
    }

    @PatchMapping("/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveAllowanceResponseDto> amend(@PathVariable UUID staffId,
                                                           @Valid @RequestBody AmendLeaveAllowanceDto dto) {
        return ResponseEntity.ok(amendLeaveAllowanceService.amend(staffId, dto.entitledDays()));
    }
}