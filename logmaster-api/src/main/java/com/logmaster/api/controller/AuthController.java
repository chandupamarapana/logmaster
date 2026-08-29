package com.logmaster.api.controller;

import com.logmaster.api.config.JwtUtil;
import com.logmaster.api.model.Role;
import com.logmaster.api.model.User;
import com.logmaster.api.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.logmaster.api.model.Company;
import com.logmaster.api.repo.CompanyRepository;
import com.logmaster.api.model.Company;
import com.logmaster.api.repo.CompanyRepository;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.getOrDefault("role", "OPERATOR");
        String companyName = body.get("companyName");

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "Username already exists"));
        }

        if (companyName == null || companyName.isBlank()) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "companyName is required"));
        }

        // Find existing company or create new one
        Company company = companyRepository.findByName(companyName)
                .orElseGet(() -> {
                    Company newCompany = new Company();
                    newCompany.setName(companyName);
                    return companyRepository.save(newCompany);
                });

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.valueOf(role));
        user.setCompany(company);
        userRepository.save(user);

        return ResponseEntity.status(201)
                .body(Map.of("message", "User registered successfully",
                        "companyId", company.getId().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(username, user.getRole().name(), user.getCompany().getId());
        return ResponseEntity.ok(Map.of("token", token));
    }
}