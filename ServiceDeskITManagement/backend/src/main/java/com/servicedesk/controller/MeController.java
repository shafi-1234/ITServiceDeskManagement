package com.servicedesk.controller;

import com.servicedesk.config.AuthUtil;
import com.servicedesk.model.AppUser;
import com.servicedesk.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {
    private final UserRepository users;

    public MeController(UserRepository users) { this.users = users; }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        AppUser u = users.findByEmail(AuthUtil.email(auth))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return Map.of("email", u.getEmail(), "name", String.valueOf(u.getName()),
                "picture", u.getPictureUrl() == null ? "" : u.getPictureUrl(),
                "role", u.getRole().name());
    }
}
