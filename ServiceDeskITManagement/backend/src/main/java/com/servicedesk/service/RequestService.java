package com.servicedesk.service;

import com.servicedesk.dto.RequestDto;
import com.servicedesk.dto.RequestForm;
import com.servicedesk.dto.StatusUpdate;
import com.servicedesk.model.AppUser;
import com.servicedesk.model.ServiceRequest;
import com.servicedesk.model.Status;
import com.servicedesk.repository.RequestRepository;
import com.servicedesk.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RequestService {
    private final RequestRepository requests;
    private final UserRepository users;

    public RequestService(RequestRepository requests, UserRepository users) {
        this.requests = requests;
        this.users = users;
    }

    // ---------- user operations ----------

    @Transactional(readOnly = true)
    public List<RequestDto> listMine(String email) {
        return requests.findByCreatedByEmailOrderByCreatedAtDesc(email).stream().map(RequestDto::from).toList();
    }

    @Transactional(readOnly = true)
    public RequestDto getMine(Long id, String email) {
        return RequestDto.from(owned(id, email));
    }

    public RequestDto create(RequestForm f, String email) {
        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        ServiceRequest r = new ServiceRequest();
        r.setTitle(f.title().trim());
        r.setDescription(f.description().trim());
        r.setCategory(f.category().trim());
        r.setPriority(f.priority());
        r.setStatus(Status.OPEN);
        r.setCreatedBy(user);
        return RequestDto.from(requests.save(r));
    }

    public RequestDto update(Long id, RequestForm f, String email) {
        ServiceRequest r = owned(id, email);
        if (r.getStatus() == Status.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Resolved requests must be reopened before editing");
        }
        r.setTitle(f.title().trim());
        r.setDescription(f.description().trim());
        r.setCategory(f.category().trim());
        r.setPriority(f.priority());
        return RequestDto.from(requests.save(r));
    }

    public void delete(Long id, String email) {
        requests.delete(owned(id, email));
    }

    public RequestDto reopen(Long id, String email) {
        ServiceRequest r = owned(id, email);
        if (r.getStatus() != Status.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only resolved requests can be reopened");
        }
        r.setStatus(Status.REOPENED);
        r.setReopenCount(r.getReopenCount() + 1);
        r.setResolvedAt(null);
        r.setResolvedBy(null);
        return RequestDto.from(requests.save(r));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> statsMine(String email) {
        Map<String, Long> m = new LinkedHashMap<>();
        for (Status s : Status.values()) m.put(s.name(), requests.countByCreatedByEmailAndStatus(email, s));
        return m;
    }

    // ---------- admin operations ----------

    @Transactional(readOnly = true)
    public List<RequestDto> listAll(Status status) {
        var list = status == null ? requests.findAllByOrderByCreatedAtDesc()
                                  : requests.findByStatusOrderByCreatedAtDesc(status);
        return list.stream().map(RequestDto::from).toList();
    }

    public RequestDto updateStatus(Long id, StatusUpdate u, String adminEmail) {
        ServiceRequest r = requests.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        r.setStatus(u.status());
        r.setAdminComment(u.comment());
        if (u.status() == Status.RESOLVED) {
            r.setResolvedAt(Instant.now());
            r.setResolvedBy(adminEmail);
        } else {
            r.setResolvedAt(null);
            r.setResolvedBy(null);
        }
        return RequestDto.from(requests.save(r));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> statsAll() {
        Map<String, Long> m = new LinkedHashMap<>();
        for (Status s : Status.values()) m.put(s.name(), requests.countByStatus(s));
        return m;
    }

    // ---------- helpers ----------

    private ServiceRequest owned(Long id, String email) {
        ServiceRequest r = requests.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!r.getCreatedBy().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }
        return r;
    }
}
