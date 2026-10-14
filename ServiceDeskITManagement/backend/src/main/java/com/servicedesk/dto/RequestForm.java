package com.servicedesk.dto;

import com.servicedesk.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RequestForm(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 100) String category,
        @NotNull Priority priority) {
}
