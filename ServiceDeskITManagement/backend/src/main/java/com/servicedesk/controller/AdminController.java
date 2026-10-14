package com.servicedesk.controller;

import com.servicedesk.config.AuthUtil;
import com.servicedesk.dto.RequestDto;
import com.servicedesk.dto.StatusUpdate;
import com.servicedesk.model.Status;
import com.servicedesk.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Admin-only endpoints (SecurityConfig: /api/admin/** requires ROLE_ADMIN). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final RequestService service;

    public AdminController(RequestService service) { this.service = service; }

    @GetMapping("/requests")
    public List<RequestDto> all(@RequestParam(required = false) Status status) { return service.listAll(status); }

    @GetMapping("/stats")
    public Map<String, Long> stats() { return service.statsAll(); }

    @PutMapping("/requests/{id}/status")
    public RequestDto updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdate update, Authentication a) {
        return service.updateStatus(id, update, AuthUtil.email(a));
    }
}
