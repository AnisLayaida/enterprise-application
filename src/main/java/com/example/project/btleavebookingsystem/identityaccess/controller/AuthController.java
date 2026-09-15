package com.example.project.btleavebookingsystem.identityaccess.controller;

import com.example.project.btleavebookingsystem.identityaccess.dto.CurrentUserResponseDto;
import com.example.project.btleavebookingsystem.identityaccess.dto.LoginRequestDto;
import com.example.project.btleavebookingsystem.identityaccess.dto.LoginResponseDto;
import com.example.project.btleavebookingsystem.identityaccess.security.AuthenticatedUser;
import com.example.project.btleavebookingsystem.identityaccess.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponseDto> me(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(authority -> authority.replace("ROLE_", ""))
                .orElse("UNKNOWN");
        return ResponseEntity.ok(new CurrentUserResponseDto(user.staffId(), user.username(), role));
    }
}