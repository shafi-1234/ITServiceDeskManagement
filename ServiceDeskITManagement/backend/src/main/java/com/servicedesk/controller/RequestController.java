package com.servicedesk.controller;

import com.servicedesk.config.AuthUtil;
import com.servicedesk.dto.RequestDto;
import com.servicedesk.dto.RequestForm;
import com.servicedesk.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Endpoints for the logged-in user's own requests (works for Google and username/password logins). */
@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestService service;

    public RequestController(RequestService service) { this.service = service; }

    @GetMapping
    public List<RequestDto> list(Authentication a) { return service.listMine(AuthUtil.email(a)); }

    @GetMapping("/stats")
    public Map<String, Long> stats(Authentication a) { return service.statsMine(AuthUtil.email(a)); }

    @GetMapping("/{id}")
    public RequestDto get(@PathVariable Long id, Authentication a) {
        return service.getMine(id, AuthUtil.email(a));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto create(@Valid @RequestBody RequestForm form, Authentication a) {
        return service.create(form, AuthUtil.email(a));
    }

    @PutMapping("/{id}")
    public RequestDto update(@PathVariable Long id, @Valid @RequestBody RequestForm form, Authentication a) {
        return service.update(id, form, AuthUtil.email(a));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication a) {
        service.delete(id, AuthUtil.email(a));
    }

    @PostMapping("/{id}/reopen")
    public RequestDto reopen(@PathVariable Long id, Authentication a) {
        return service.reopen(id, AuthUtil.email(a));
    }
}
