package com.servicedesk.dto;

import com.servicedesk.model.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusUpdate(@NotNull Status status, @Size(max = 1000) String comment) {
}
