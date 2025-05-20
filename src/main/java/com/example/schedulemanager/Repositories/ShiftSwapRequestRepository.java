package com.example.schedulemanager.Repositories;


import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.ShiftSwapRequest;
import com.example.schedulemanager.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {
    List<ShiftSwapRequest> findByRequester(User requester);
    List<ShiftSwapRequest> findByTargetUser(User targetUser);
  //  void deleteByShiftIdIn(List<Long> shiftIds);
    List<ShiftSwapRequest> findByRequestedShiftOrDesiredShift(Shift requested, Shift desired);


}
