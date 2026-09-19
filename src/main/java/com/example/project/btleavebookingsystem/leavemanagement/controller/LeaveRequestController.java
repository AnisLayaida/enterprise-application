package com.example.project.btleavebookingsystem.leavemanagement.controller;

import com.example.project.btleavebookingsystem.identityaccess.security.AuthenticatedUser;
import com.example.project.btleavebookingsystem.leavemanagement.access.ActingUser;
import com.example.project.btleavebookingsystem.leavemanagement.dto.*;
import com.example.project.btleavebookingsystem.leavemanagement.service.command.*;
import com.example.project.btleavebookingsystem.leavemanagement.service.query.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final SubmitLeaveRequestService submitLeaveRequestService;
    private final ReviewLeaveRequestService reviewLeaveRequestService;
    private final CancelLeaveRequestService cancelLeaveRequestService;
    private final ResolveHRReviewService resolveHRReviewService;
    private final GetMyLeaveRequestsService getMyLeaveRequestsService;
    private final GetTeamPendingRequestsService getTeamPendingRequestsService;
    private final GetOutstandingRequestsService getOutstandingRequestsService;
    private final GetLeaveRequestByIdService getLeaveRequestByIdService;
    private final GetLeaveRequestHistoryService getLeaveRequestHistoryService;

    public LeaveRequestController(SubmitLeaveRequestService submitLeaveRequestService,
                                  ReviewLeaveRequestService reviewLeaveRequestService,
                                  CancelLeaveRequestService cancelLeaveRequestService,
                                  ResolveHRReviewService resolveHRReviewService,
                                  GetMyLeaveRequestsService getMyLeaveRequestsService,
                                  GetTeamPendingRequestsService getTeamPendingRequestsService,
                                  GetOutstandingRequestsService getOutstandingRequestsService,
                                  GetLeaveRequestByIdService getLeaveRequestByIdService,
                                  GetLeaveRequestHistoryService getLeaveRequestHistoryService) {
        this.submitLeaveRequestService = submitLeaveRequestService;
        this.reviewLeaveRequestService = reviewLeaveRequestService;
        this.cancelLeaveRequestService = cancelLeaveRequestService;
        this.resolveHRReviewService = resolveHRReviewService;
        this.getMyLeaveRequestsService = getMyLeaveRequestsService;
        this.getTeamPendingRequestsService = getTeamPendingRequestsService;
        this.getOutstandingRequestsService = getOutstandingRequestsService;
        this.getLeaveRequestByIdService = getLeaveRequestByIdService;
        this.getLeaveRequestHistoryService = getLeaveRequestHistoryService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponseDto> submit(@Valid @RequestBody SubmitLeaveRequestDto dto,
                                                          Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(submitLeaveRequestService.submit(user.staffId(), dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestResponseDto> getById(@PathVariable UUID id,
                                                           Authentication authentication) {
        return ResponseEntity.ok(getLeaveRequestByIdService.getById(id, ActingUser.from(authentication)));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestHistoryResponseDto> getHistory(@PathVariable UUID id,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(getLeaveRequestHistoryService.getHistory(id, ActingUser.from(authentication)));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestResponseDto> review(@PathVariable UUID id,
                                                          @Valid @RequestBody ReviewLeaveRequestDto dto,
                                                          Authentication authentication) {
        return ResponseEntity.ok(reviewLeaveRequestService.review(
                id, dto.approved(), ActingUser.from(authentication)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestResponseDto> cancel(@PathVariable UUID id,
                                                          Authentication authentication) {
        return ResponseEntity.ok(cancelLeaveRequestService.cancel(id, ActingUser.from(authentication)));
    }

    @PatchMapping("/{id}/hr-review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponseDto> resolveHRReview(@PathVariable UUID id,
                                                                   @Valid @RequestBody ReviewLeaveRequestDto dto,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(resolveHRReviewService.resolve(
                id, dto.approved(), ActingUser.from(authentication)));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<LeaveRequestResponseDto>> getMine(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(getMyLeaveRequestsService.getForStaff(user.staffId()));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<LeaveRequestResponseDto>> getTeam(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(getTeamPendingRequestsService.getForManager(user.staffId(), startDate, endDate));
    }

    /**
     * Administrator view of outstanding requests: company-wide by default,
     * optionally filtered by staff member and/or by a manager's team (filters combine with AND).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestResponseDto>> getOutstanding(
            @RequestParam(required = false) UUID staffId,
            @RequestParam(required = false) UUID managerId) {
        return ResponseEntity.ok(getOutstandingRequestsService.getOutstanding(staffId, managerId));
    }
}