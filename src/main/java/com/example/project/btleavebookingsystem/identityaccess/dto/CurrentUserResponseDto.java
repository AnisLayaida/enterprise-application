package com.example.project.btleavebookingsystem.identityaccess.dto;

import java.util.UUID;

public record CurrentUserResponseDto(UUID staffId, String username, String role) {
}