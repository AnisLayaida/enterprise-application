package com.example.project.btleavebookingsystem.leavemanagement.access;

import com.example.project.btleavebookingsystem.identityaccess.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public record ActingUser(UUID staffId, boolean admin) {

    public static ActingUser from(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return new ActingUser(principal.staffId(), admin);
    }
}