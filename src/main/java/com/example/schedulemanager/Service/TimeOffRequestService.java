package com.example.schedulemanager.Service;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.TimeOffRequest;
import com.example.schedulemanager.Repositories.TimeOffRequestRepository;
import com.example.schedulemanager.Repositories.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.schedulemanager.Enum.ApprovalStatus;

import java.util.List;
import java.util.Optional;

@Service
public class TimeOffRequestService {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final ShiftRepository shiftRepository;

    @Autowired
    public TimeOffRequestService(TimeOffRequestRepository timeOffRequestRepository, ShiftRepository shiftRepository) {
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.shiftRepository = shiftRepository;
    }

    // Skapa en ny TimeOffRequest
    public TimeOffRequest createRequest(TimeOffRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Slutdatum kan inte vara före startdatum.");
        }

        if (request.getApprovalStatus() == null) {
            request.setApprovalStatus(ApprovalStatus.PENDING);
        }

        List<Shift> overlappingShifts = shiftRepository.findByAssignedUserIdAndDateBetween(
                request.getUser().getId(), request.getStartDate(), request.getEndDate());

        if (!overlappingShifts.isEmpty()) {
            throw new RuntimeException("Användaren har redan skift inplanerade under begärd ledighet.");
        }

        return timeOffRequestRepository.save(request);
    }

    // Hämta alla TimeOffRequest
    public List<TimeOffRequest> getAllRequests() {
        return timeOffRequestRepository.findAll();
    }

    // Hämta en TimeOffRequest via ID
    public Optional<TimeOffRequest> getRequestById(Long id) {
        return timeOffRequestRepository.findById(id);
    }

    // Hämta TimeOffRequests för en viss användare
    public List<TimeOffRequest> getRequestsByUserId(Long userId) {
        return timeOffRequestRepository.findByUserId(userId);
    }

    // Uppdatera en TimeOffRequest
    public TimeOffRequest updateRequest(Long id, TimeOffRequest updatedRequest) {
        if (updatedRequest.getEndDate().isBefore(updatedRequest.getStartDate())) {
            throw new IllegalArgumentException("Slutdatum kan inte vara före startdatum.");
        }

        List<Shift> overlappingShifts = shiftRepository.findByAssignedUserIdAndDateBetween(
                updatedRequest.getUser().getId(), updatedRequest.getStartDate(), updatedRequest.getEndDate());

        if (!overlappingShifts.isEmpty()) {
            throw new RuntimeException("Användaren har redan skift inplanerade under begärd ledighet.");
        }

        updatedRequest.setId(id);
        return timeOffRequestRepository.save(updatedRequest);
    }

    // Ta bort en TimeOffRequest
    public boolean deleteRequest(Long id) {
        if (timeOffRequestRepository.existsById(id)) {
            timeOffRequestRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Godkänn en TimeOffRequest
    public TimeOffRequest approveRequest(Long id) {
        Optional<TimeOffRequest> requestOptional = timeOffRequestRepository.findById(id);
        if (requestOptional.isPresent()) {
            TimeOffRequest request = requestOptional.get();
            if (request.getApprovalStatus() == ApprovalStatus.PENDING) {
                request.setApprovalStatus(ApprovalStatus.APPROVED);
                return timeOffRequestRepository.save(request);
            }
            throw new RuntimeException("Begäran är redan behandlad (antingen godkänd eller avslagen).");
        }
        throw new RuntimeException("Tid off begäran inte funnen.");
    }

    // Avslå en TimeOffRequest
    public TimeOffRequest rejectRequest(Long id) {
        Optional<TimeOffRequest> requestOptional = timeOffRequestRepository.findById(id);
        if (requestOptional.isPresent()) {
            TimeOffRequest request = requestOptional.get();
            if (request.getApprovalStatus() == ApprovalStatus.PENDING) {
                request.setApprovalStatus(ApprovalStatus.REJECTED);
                return timeOffRequestRepository.save(request);
            }
            throw new RuntimeException("Begäran är redan behandlad (antingen godkänd eller avslagen).");
        }
        throw new RuntimeException("Tid off begäran inte funnen.");
    }
}
