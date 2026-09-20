package com.example.project.btleavebookingsystem.identityaccess.dto;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleDto(@NotNull Role.RoleName role) {
}