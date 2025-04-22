package com.example.schedulemanager.Repositories;

import com.example.schedulemanager.Entities.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Long> {
    List<TimeOffRequest> findByUserId(Long userId);
}
