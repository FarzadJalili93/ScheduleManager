package com.example.schedulemanager.Repositories;

import com.example.schedulemanager.Entities.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Long> {

    List<TimeOffRequest> findByUserId(Long userId);

    @Query("SELECT r FROM TimeOffRequest r WHERE r.user.id = :userId AND " +
            "(:startDate <= r.endDate AND :endDate >= r.startDate)")
    List<TimeOffRequest> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT r FROM TimeOffRequest r WHERE r.user.id = :userId AND " +
            "(:startDate <= r.endDate AND :endDate >= r.startDate) AND r.id <> :excludeId")
    List<TimeOffRequest> findByUserIdAndDateRangeExcludingId(@Param("userId") Long userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate,
                                                             @Param("excludeId") Long excludeId);
}
