package com.example.schedulemanager.Service;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.ShiftSwapRequest;
import com.example.schedulemanager.Entities.TimeOffRequest;
import com.example.schedulemanager.Repositories.ShiftSwapRequestRepository;
import com.example.schedulemanager.Repositories.TimeOffRequestRepository;
import com.example.schedulemanager.Repositories.ShiftRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.schedulemanager.Enum.ApprovalStatus;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TimeOffRequestService {

    private static final Logger logger = LoggerFactory.getLogger(TimeOffRequestService.class);

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftSwapRequestRepository shiftSwapRequestRepository;
    private final ShiftService shiftService;

    @Autowired
    public TimeOffRequestService(TimeOffRequestRepository timeOffRequestRepository, ShiftRepository shiftRepository, ShiftService shiftService, ShiftSwapRequestRepository shiftSwapRequestRepository) {
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.shiftRepository = shiftRepository;
        this.shiftService = shiftService;
        this.shiftSwapRequestRepository = shiftSwapRequestRepository;

    }

    public TimeOffRequest createRequest(TimeOffRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Slutdatum kan inte vara före startdatum.");
        }

        if (request.getApprovalStatus() == null) {
            request.setApprovalStatus(ApprovalStatus.PENDING);
        }

        List<TimeOffRequest> overlappingRequests = timeOffRequestRepository.findByUserIdAndDateRange(
                request.getUser().getId(), request.getStartDate(), request.getEndDate());

        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("Användaren har redan en ledighetsförfrågan under denna period.");
        }

        return timeOffRequestRepository.save(request);
    }

    public List<TimeOffRequest> getAllRequests() {
        return timeOffRequestRepository.findAll();
    }

    public Optional<TimeOffRequest> getRequestById(Long id) {
        return timeOffRequestRepository.findById(id);
    }

    public List<TimeOffRequest> getRequestsByUserId(Long userId) {
        return timeOffRequestRepository.findByUserId(userId);
    }

    public TimeOffRequest approveRequest(Long id) {
        logger.info("Försöker godkänna ledighetsansökan med ID: {}", id);

        Optional<TimeOffRequest> requestOptional = timeOffRequestRepository.findById(id);
        if (requestOptional.isEmpty()) {
            logger.error("Ledighetsansökan med ID {} hittades inte", id);
            throw new RuntimeException("Tid off begäran inte funnen.");
        }

        TimeOffRequest request = requestOptional.get();

        logger.info("Hittade ledighetsansökan för användare ID: {} från {} till {}",
                request.getUser().getId(), request.getStartDate(), request.getEndDate());

        if (request.getApprovalStatus() != ApprovalStatus.PENDING) {
            logger.warn("Ansökan med ID {} är redan behandlad med status: {}", id, request.getApprovalStatus());
            throw new RuntimeException("Begäran är redan behandlad.");
        }


        request.setApprovalStatus(ApprovalStatus.APPROVED);
        TimeOffRequest savedRequest = timeOffRequestRepository.save(request);
        logger.info("Ansökan godkänd och sparad.");


        List<Shift> shifts = shiftRepository.findByAssignedUserIdAndDateBetween(
                request.getUser().getId(),
                request.getStartDate(),
                request.getEndDate());

        logger.info("Antal skift som hittades: {}", shifts.size());
        for (Shift shift : shifts) {
            logger.info("Skift ID: {}, Datum: {}", shift.getId(), shift.getDate());

            List<ShiftSwapRequest> relatedSwapRequests =
                    shiftSwapRequestRepository.findByRequestedShiftOrDesiredShift(shift, shift);

            if (!relatedSwapRequests.isEmpty()) {
                logger.info("Tar bort {} relaterade shift swap requests för skift ID {}", relatedSwapRequests.size(), shift.getId());
                shiftSwapRequestRepository.deleteAll(relatedSwapRequests);
            }
        }

        shiftRepository.deleteAll(shifts);
        logger.info("Skift raderade.");

        return savedRequest;
    }


    public TimeOffRequest updateRequest(Long id, TimeOffRequest updatedRequest) {
        if (updatedRequest.getEndDate().isBefore(updatedRequest.getStartDate())) {
            throw new IllegalArgumentException("Slutdatum kan inte vara före startdatum.");
        }

        List<TimeOffRequest> overlappingRequests = timeOffRequestRepository.findByUserIdAndDateRangeExcludingId(
                updatedRequest.getUser().getId(), updatedRequest.getStartDate(), updatedRequest.getEndDate(), id);

        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("Användaren har redan en ledighetsförfrågan under denna period.");
        }

        updatedRequest.setId(id);
        return timeOffRequestRepository.save(updatedRequest);
    }

    public boolean deleteRequest(Long id) {
        if (timeOffRequestRepository.existsById(id)) {
            timeOffRequestRepository.deleteById(id);
            return true;
        }
        return false;
    }
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
