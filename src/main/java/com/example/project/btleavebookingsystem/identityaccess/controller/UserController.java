package com.example.project.btleavebookingsystem.identityaccess.controller;

import com.example.project.btleavebookingsystem.identityaccess.dto.ChangeRoleDto;
import com.example.project.btleavebookingsystem.identityaccess.dto.UserRoleResponseDto;
import com.example.project.btleavebookingsystem.identityaccess.security.AuthenticatedUser;
import com.example.project.btleavebookingsystem.identityaccess.service.ChangeUserRoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ChangeUserRoleService changeUserRoleService;

    public UserController(ChangeUserRoleService changeUserRoleService) {
        this.changeUserRoleService = changeUserRoleService;
    }

    @PatchMapping("/{staffId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserRoleResponseDto> changeRole(@PathVariable UUID staffId,
                                                          @Valid @RequestBody ChangeRoleDto dto,
                                                          Authentication authentication) {
        AuthenticatedUser admin = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(changeUserRoleService.changeRole(staffId, dto.role(), admin.staffId()));
    }
}