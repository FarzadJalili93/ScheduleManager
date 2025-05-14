package com.example.schedulemanager.Repositories;

import com.example.schedulemanager.Entities.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByAssignedUserId(Long userId); // Hämtar alla skift för en viss användare

    List<Shift> findByDate(LocalDate date); // Hämtar alla skift för ett specifikt datum

    List<Shift> findByAssignedUserIdAndDate(Long userId, LocalDate date); // Hämtar skift för en användare på ett specifikt datum

    List<Shift> findByAssignedUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Shift> findByAssignedUserIdNot(Long userId);

}
