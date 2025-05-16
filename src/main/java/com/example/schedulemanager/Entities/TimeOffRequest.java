package com.example.schedulemanager.Entities;

import com.example.schedulemanager.Enum.ApprovalStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class TimeOffRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public TimeOffRequest() {}

    public TimeOffRequest(LocalDate startDate, LocalDate endDate, String reason, ApprovalStatus approvalStatus, User user) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.approvalStatus = approvalStatus;
        this.user = user;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
