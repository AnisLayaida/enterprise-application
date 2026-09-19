package com.example.project.btleavebookingsystem.identityaccess.dto;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;

import java.util.UUID;

public record RoleResponseDto(UUID id, String name) {
    public static RoleResponseDto from(Role role) {
        return new RoleResponseDto(role.getId(), role.getName().name());
    }
}