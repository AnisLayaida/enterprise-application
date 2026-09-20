package com.example.project.btleavebookingsystem.identityaccess.dto;

import com.example.project.btleavebookingsystem.identityaccess.domain.User;

import java.util.UUID;

public record UserRoleResponseDto(UUID staffId, String username, String role) {

    public static UserRoleResponseDto from(User user) {
        return new UserRoleResponseDto(user.getStaffId(), user.getUsername(), user.getRole().getName().name());
    }
}