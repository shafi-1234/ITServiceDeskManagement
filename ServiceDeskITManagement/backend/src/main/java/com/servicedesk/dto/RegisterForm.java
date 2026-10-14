package com.servicedesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterForm(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_.-]{3,30}$", message = "Username must be 3-30 letters, digits, . _ -") String username,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password) {
}
