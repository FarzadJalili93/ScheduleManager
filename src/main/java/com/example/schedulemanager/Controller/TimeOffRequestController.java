package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.TimeOffRequest;
import com.example.schedulemanager.Service.TimeOffRequestService;
import com.example.schedulemanager.Enum.ApprovalStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/timeoffrequests")
public class TimeOffRequestController {

    private final TimeOffRequestService timeOffRequestService;

    @Autowired
    public TimeOffRequestController(TimeOffRequestService timeOffRequestService) {
        this.timeOffRequestService = timeOffRequestService;
    }

    // Skapa en ny TimeOffRequest
    @PostMapping
    public ResponseEntity<TimeOffRequest> createRequest(@RequestBody TimeOffRequest request) {
        try {
            TimeOffRequest createdRequest = timeOffRequestService.createRequest(request);
            return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Hämta alla TimeOffRequests
    @GetMapping
    public ResponseEntity<List<TimeOffRequest>> getAllRequests() {
        List<TimeOffRequest> requests = timeOffRequestService.getAllRequests();
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    // Hämta en TimeOffRequest via ID
    @GetMapping("/{id}")
    public ResponseEntity<TimeOffRequest> getRequestById(@PathVariable Long id) {
        Optional<TimeOffRequest> request = timeOffRequestService.getRequestById(id);
        return request.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Hämta TimeOffRequests för en viss användare
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TimeOffRequest>> getRequestsByUserId(@PathVariable Long userId) {
        List<TimeOffRequest> requests = timeOffRequestService.getRequestsByUserId(userId);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    // Uppdatera en TimeOffRequest
    @PutMapping("/{id}")
    public ResponseEntity<TimeOffRequest> updateRequest(@PathVariable Long id, @RequestBody TimeOffRequest updatedRequest) {
        TimeOffRequest request = timeOffRequestService.updateRequest(id, updatedRequest);
        return request != null ? new ResponseEntity<>(request, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Ta bort en TimeOffRequest
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        boolean deleted = timeOffRequestService.deleteRequest(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Uppdatera godkännande-status för en TimeOffRequest (approve/deny)
    @PutMapping("/{id}/status")
    public ResponseEntity<TimeOffRequest> updateApprovalStatus(@PathVariable Long id, @RequestParam ApprovalStatus status) {
        try {
            TimeOffRequest updatedRequest;
            if (status == ApprovalStatus.APPROVED) {
                updatedRequest = timeOffRequestService.approveRequest(id); // Använd service-metod för godkännande
            } else if (status == ApprovalStatus.REJECTED) {
                updatedRequest = timeOffRequestService.rejectRequest(id); // Använd service-metod för avslående
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Ogiltig status
            }
            return new ResponseEntity<>(updatedRequest, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Om något gick fel, returnera NOT_FOUND
        }
    }
}
