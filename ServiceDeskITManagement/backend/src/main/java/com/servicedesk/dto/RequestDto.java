package com.servicedesk.dto;

import com.servicedesk.model.Priority;
import com.servicedesk.model.ServiceRequest;
import com.servicedesk.model.Status;

import java.time.Instant;

public record RequestDto(
        Long id, String title, String description, String category,
        Priority priority, Status status,
        String createdByName, String createdByEmail,
        String resolvedBy, String adminComment, int reopenCount,
        Instant createdAt, Instant updatedAt, Instant resolvedAt) {

    public static RequestDto from(ServiceRequest r) {
        return new RequestDto(r.getId(), r.getTitle(), r.getDescription(), r.getCategory(),
                r.getPriority(), r.getStatus(),
                r.getCreatedBy().getName(), r.getCreatedBy().getEmail(),
                r.getResolvedBy(), r.getAdminComment(), r.getReopenCount(),
                r.getCreatedAt(), r.getUpdatedAt(), r.getResolvedAt());
    }
}
