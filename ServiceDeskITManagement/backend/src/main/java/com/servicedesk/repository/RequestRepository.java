package com.servicedesk.repository;

import com.servicedesk.model.ServiceRequest;
import com.servicedesk.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByCreatedByEmailOrderByCreatedAtDesc(String email);
    List<ServiceRequest> findAllByOrderByCreatedAtDesc();
    List<ServiceRequest> findByStatusOrderByCreatedAtDesc(Status status);
    long countByStatus(Status status);
    long countByCreatedByEmailAndStatus(String email, Status status);
}
