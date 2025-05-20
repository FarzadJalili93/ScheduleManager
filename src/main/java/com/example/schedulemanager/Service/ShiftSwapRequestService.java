package com.example.schedulemanager.Service;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.ShiftSwapRequest;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.ShiftRepository;
import com.example.schedulemanager.Repositories.ShiftSwapRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftSwapRequestService {

    private final ShiftSwapRequestRepository shiftSwapRequestRepository;
    private final ShiftRepository shiftRepository;

    public ShiftSwapRequestService(ShiftSwapRequestRepository shiftSwapRequestRepository, ShiftRepository shiftRepository) {
        this.shiftSwapRequestRepository = shiftSwapRequestRepository;
        this.shiftRepository = shiftRepository;
    }

    public ShiftSwapRequest createSwapRequest(ShiftSwapRequest request) {
        if (request.getTargetUser() == null) {
            throw new IllegalArgumentException("Mottagaren av bytet får inte vara tom.");
        }
        return shiftSwapRequestRepository.save(request);
    }

    public List<ShiftSwapRequest> getAllRequests() {
        return shiftSwapRequestRepository.findAll();
    }

    public void deleteRequest(Long id) {
        shiftSwapRequestRepository.deleteById(id);
    }

    public ShiftSwapRequest approveRequest(Long requestId) {
        Optional<ShiftSwapRequest> optional = shiftSwapRequestRepository.findById(requestId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Förfrågan kunde inte hittas.");
        }

        ShiftSwapRequest request = optional.get();
        Shift requestedShift = request.getRequestedShift();
        Shift desiredShift = request.getDesiredShift();

        User requester = request.getRequester();
        User targetUser = request.getTargetUser();

        requestedShift.setAssignedUser(targetUser);
        desiredShift.setAssignedUser(requester);

        shiftRepository.save(requestedShift);
        shiftRepository.save(desiredShift);

        request.setStatus("APPROVED");
        return shiftSwapRequestRepository.save(request);
    }

    public ShiftSwapRequest declineRequest(Long requestId) {
        ShiftSwapRequest request = shiftSwapRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Förfrågan hittades inte"));

        request.setStatus("DECLINED");
        return shiftSwapRequestRepository.save(request);
    }
}
