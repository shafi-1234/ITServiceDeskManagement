package com.servicedesk.controller;

import com.servicedesk.dto.RegisterForm;
import com.servicedesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;

    public AuthController(UserService users) { this.users = users; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterForm f) {
        users.registerLocal(f.username(), f.email(), f.name(), f.password());
        return Map.of("status", "registered");
    }
}
