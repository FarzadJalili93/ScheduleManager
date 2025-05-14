package com.example.schedulemanager.Entities;

import jakarta.persistence.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ShiftSwapRequest {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester; // Den som vill byta bort sitt skift

    @ManyToOne
    @JoinColumn(name = "requested_shift_id")
    private Shift requestedShift; // Skiftet som användaren vill byta bort

    @ManyToOne
    @JoinColumn(name = "desired_shift_id")
    private Shift desiredShift; // Skiftet som användaren vill få

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser; // Den användare som man vill byta skift med

    private LocalDateTime requestDate;

    private String status; // t.ex. "PENDING", "APPROVED", "DECLINED"

    public ShiftSwapRequest() {}

    public ShiftSwapRequest(User requester, Shift requestedShift, Shift desiredShift, User targetUser, LocalDateTime requestDate, String status) {
        this.requester = requester;
        this.requestedShift = requestedShift;
        this.desiredShift = desiredShift;
        this.targetUser = targetUser;
        this.requestDate = requestDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public Shift getRequestedShift() {
        return requestedShift;
    }

    public void setRequestedShift(Shift requestedShift) {
        this.requestedShift = requestedShift;
    }

    public Shift getDesiredShift() {
        return desiredShift;
    }

    public void setDesiredShift(Shift desiredShift) {
        this.desiredShift = desiredShift;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ShiftSwapRequest{" +
                "id=" + id +
                ", requester=" + requester.getId() +
                ", requestedShift=" + requestedShift.getId() +
                ", desiredShift=" + desiredShift.getId() +
                ", targetUser=" + targetUser.getId() +
                ", requestDate=" + requestDate +
                ", status='" + status + '\'' +
                '}';
    }
}


