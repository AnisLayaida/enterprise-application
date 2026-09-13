package com.example.project.btleavebookingsystem.staffmanagement.controller;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;
import com.example.project.btleavebookingsystem.identityaccess.domain.User;
import com.example.project.btleavebookingsystem.identityaccess.repository.RoleRepository;
import com.example.project.btleavebookingsystem.identityaccess.repository.UserRepository;
import com.example.project.btleavebookingsystem.leavemanagement.domain.LeaveAllowance;
import com.example.project.btleavebookingsystem.leavemanagement.repository.LeaveAllowanceRepository;
import com.example.project.btleavebookingsystem.shared.exception.ResourceNotFoundException;
import com.example.project.btleavebookingsystem.staffmanagement.domain.StaffMember;
import com.example.project.btleavebookingsystem.staffmanagement.dto.AddStaffMemberDto;
import com.example.project.btleavebookingsystem.staffmanagement.dto.AmendStaffMemberDto;
import com.example.project.btleavebookingsystem.staffmanagement.dto.StaffMemberResponseDto;
import com.example.project.btleavebookingsystem.staffmanagement.repository.StaffMemberRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private static final int DEFAULT_ANNUAL_ENTITLEMENT_DAYS = 25;

    private final StaffMemberRepository staffMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LeaveAllowanceRepository leaveAllowanceRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffController(StaffMemberRepository staffMemberRepository, UserRepository userRepository,
                           RoleRepository roleRepository, LeaveAllowanceRepository leaveAllowanceRepository,
                           PasswordEncoder passwordEncoder) {
        this.staffMemberRepository = staffMemberRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.leaveAllowanceRepository = leaveAllowanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<StaffMemberResponseDto> addStaffMember(@Valid @RequestBody AddStaffMemberDto dto) {
        StaffMember staffMember = new StaffMember(
                UUID.randomUUID(), dto.firstName(), dto.surname(), dto.email(),
                dto.hireDate(), dto.department(), dto.lineManagerId(),
                dto.jobLevel(), dto.employmentStatus());
        staffMemberRepository.save(staffMember);

        Role role = roleRepository.findByName(dto.role())
                .orElseThrow(() -> new IllegalStateException("Role not seeded: " + dto.role()));

        User user = new User(staffMember.getId(), dto.username(),
                passwordEncoder.encode(dto.password()), role);
        userRepository.save(user);

        LeaveAllowance allowance = new LeaveAllowance(
                staffMember.getId(), Year.now().getValue(), DEFAULT_ANNUAL_ENTITLEMENT_DAYS);
        leaveAllowanceRepository.save(allowance);

        return ResponseEntity.status(HttpStatus.CREATED).body(StaffMemberResponseDto.from(staffMember));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffMemberResponseDto>> getAll() {
        List<StaffMemberResponseDto> staff = staffMemberRepository.findAll().stream()
                .map(StaffMemberResponseDto::from)
                .toList();
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StaffMemberResponseDto> getById(@PathVariable UUID id) {
        StaffMember staffMember = staffMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found: " + id));
        return ResponseEntity.ok(StaffMemberResponseDto.from(staffMember));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<StaffMemberResponseDto> amend(@PathVariable UUID id,
                                                        @Valid @RequestBody AmendStaffMemberDto dto) {
        StaffMember staffMember = staffMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found: " + id));

        staffMember.applyUpdate(dto.department(), dto.jobLevel(), dto.employmentStatus());
        staffMemberRepository.save(staffMember);

        return ResponseEntity.ok(StaffMemberResponseDto.from(staffMember));
    }
}