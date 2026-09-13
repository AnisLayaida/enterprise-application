package com.example.project.btleavebookingsystem.identityaccess.security;

import java.util.UUID;

public record AuthenticatedUser(UUID staffId, String username) {
}