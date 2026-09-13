package com.example.project.btleavebookingsystem.shared.config;

import com.example.project.btleavebookingsystem.identityaccess.domain.Role;
import com.example.project.btleavebookingsystem.identityaccess.domain.User;
import com.example.project.btleavebookingsystem.identityaccess.repository.RoleRepository;
import com.example.project.btleavebookingsystem.identityaccess.repository.UserRepository;
import com.example.project.btleavebookingsystem.staffmanagement.domain.StaffMember;
import com.example.project.btleavebookingsystem.staffmanagement.repository.StaffMemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, StaffMemberRepository staffMemberRepository,
                      UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN).orElseThrow();

            StaffMember admin = new StaffMember(
                    UUID.randomUUID(), "System", "Administrator", "admin@bt.com",
                    LocalDate.now(), "IT", null, "N/A",
                    StaffMember.EmploymentStatus.ACTIVE);
            staffMemberRepository.save(admin);

            User adminUser = new User(admin.getId(), "admin",
                    passwordEncoder.encode("AdminPass123!"), adminRole);
            userRepository.save(adminUser);
        }
    }
}