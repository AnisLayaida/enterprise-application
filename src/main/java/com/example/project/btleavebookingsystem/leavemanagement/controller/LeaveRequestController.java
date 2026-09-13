package com.example.project.btleavebookingsystem.leavemanagement.controller;

import com.example.project.btleavebookingsystem.identityaccess.security.AuthenticatedUser;
import com.example.project.btleavebookingsystem.leavemanagement.dto.LeaveRequestResponseDto;
import com.example.project.btleavebookingsystem.leavemanagement.dto.SubmitLeaveRequestDto;
import com.example.project.btleavebookingsystem.leavemanagement.service.command.SubmitLeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final SubmitLeaveRequestService submitLeaveRequestService;

    public LeaveRequestController(SubmitLeaveRequestService submitLeaveRequestService) {
        this.submitLeaveRequestService = submitLeaveRequestService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponseDto> submit(@Valid @RequestBody SubmitLeaveRequestDto dto,
                                                          Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        LeaveRequestResponseDto response = submitLeaveRequestService.submit(user.staffId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}