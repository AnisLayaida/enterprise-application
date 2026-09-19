package com.example.project.btleavebookingsystem.identityaccess.controller;

import com.example.project.btleavebookingsystem.identityaccess.dto.RoleResponseDto;
import com.example.project.btleavebookingsystem.identityaccess.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponseDto>> getAll() {
        List<RoleResponseDto> roles = roleRepository.findAll().stream()
                .map(RoleResponseDto::from)
                .toList();
        return ResponseEntity.ok(roles);
    }
}