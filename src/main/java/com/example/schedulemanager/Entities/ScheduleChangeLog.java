package com.example.schedulemanager.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ScheduleChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String action;
    private String details;

    @ManyToOne
    private User performedBy;
}

