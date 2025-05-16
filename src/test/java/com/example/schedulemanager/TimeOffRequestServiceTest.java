package com.example.schedulemanager;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.TimeOffRequest;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Enum.ApprovalStatus;
import com.example.schedulemanager.Repositories.ShiftRepository;
import com.example.schedulemanager.Repositories.TimeOffRequestRepository;
import com.example.schedulemanager.Service.TimeOffRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeOffRequestServiceTest {

    private TimeOffRequestRepository timeOffRequestRepository;
    private ShiftRepository shiftRepository;
    private TimeOffRequestService service;

    @BeforeEach
    void setup() {
        timeOffRequestRepository = mock(TimeOffRequestRepository.class);
        shiftRepository = mock(ShiftRepository.class);
        service = new TimeOffRequestService(timeOffRequestRepository, shiftRepository);
    }

    @Test
    void createRequestWithEndDateBeforeStartDateShouldThrow() {
        TimeOffRequest request = new TimeOffRequest();
        request.setStartDate(LocalDate.of(2025, 5, 10));
        request.setEndDate(LocalDate.of(2025, 5, 5));
        request.setUser(new User());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createRequest(request));

        assertEquals("Slutdatum kan inte vara före startdatum.", ex.getMessage());
    }

    @Test
    void createRequestWithOverlappingShifts() {
        TimeOffRequest request = new TimeOffRequest();
        User user = new User();
        user.setId(1L);
        request.setUser(user);
        request.setStartDate(LocalDate.of(2025, 5, 1));
        request.setEndDate(LocalDate.of(2025, 5, 5));

        when(shiftRepository.findByAssignedUserIdAndDateBetween(1L, request.getStartDate(), request.getEndDate()))
                .thenReturn(Collections.singletonList(new Shift()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createRequest(request));

        assertEquals("Användaren har redan skift inplanerade under begärd ledighet.", ex.getMessage());
    }

    @Test
    void createRequestValidRequestShouldSave() {
        TimeOffRequest request = new TimeOffRequest();
        User user = new User();
        user.setId(1L);
        request.setUser(user);
        request.setStartDate(LocalDate.of(2025, 5, 1));
        request.setEndDate(LocalDate.of(2025, 5, 5));
        request.setApprovalStatus(null);

        when(shiftRepository.findByAssignedUserIdAndDateBetween(1L, request.getStartDate(), request.getEndDate()))
                .thenReturn(Collections.emptyList());
        when(timeOffRequestRepository.save(request)).thenReturn(request);

        TimeOffRequest saved = service.createRequest(request);

        assertEquals(ApprovalStatus.PENDING, saved.getApprovalStatus());
        verify(timeOffRequestRepository).save(request);
    }

    @Test
    void approveRequestWhenPendingShouldSetApproved() {
        Long id = 1L;
        TimeOffRequest request = new TimeOffRequest();
        request.setId(id);
        request.setApprovalStatus(ApprovalStatus.PENDING);

        when(timeOffRequestRepository.findById(id)).thenReturn(Optional.of(request));
        when(timeOffRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TimeOffRequest approved = service.approveRequest(id);

        assertEquals(ApprovalStatus.APPROVED, approved.getApprovalStatus());
        verify(timeOffRequestRepository).save(request);
    }

    @Test
    void approveRequestWhenNotPendingShouldThrow() {
        Long id = 1L;
        TimeOffRequest request = new TimeOffRequest();
        request.setId(id);
        request.setApprovalStatus(ApprovalStatus.APPROVED);

        when(timeOffRequestRepository.findById(id)).thenReturn(Optional.of(request));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.approveRequest(id));

        assertEquals("Begäran är redan behandlad (antingen godkänd eller avslagen).", ex.getMessage());
    }

    @Test
    void deleteRequestWhenExistsShouldReturnTrue() {
        Long id = 1L;

        when(timeOffRequestRepository.existsById(id)).thenReturn(true);

        boolean result = service.deleteRequest(id);

        assertTrue(result);
        verify(timeOffRequestRepository).deleteById(id);
    }

    @Test
    void deleteRequestWhenNotExistsShouldReturnFalse() {
        Long id = 2L;

        when(timeOffRequestRepository.existsById(id)).thenReturn(false);

        boolean result = service.deleteRequest(id);

        assertFalse(result);
        verify(timeOffRequestRepository, never()).deleteById(anyLong());
    }

    @Test
    void rejectRequestWhenPendingShouldSetRejected() {
        Long id = 1L;
        TimeOffRequest request = new TimeOffRequest();
        request.setId(id);
        request.setApprovalStatus(ApprovalStatus.PENDING);

        when(timeOffRequestRepository.findById(id)).thenReturn(Optional.of(request));
        when(timeOffRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TimeOffRequest rejected = service.rejectRequest(id);

        assertEquals(ApprovalStatus.REJECTED, rejected.getApprovalStatus());
        verify(timeOffRequestRepository).save(request);
    }

    @Test
    void rejectRequestWhenNotPendingShouldThrow() {
        Long id = 1L;
        TimeOffRequest request = new TimeOffRequest();
        request.setId(id);
        request.setApprovalStatus(ApprovalStatus.APPROVED);

        when(timeOffRequestRepository.findById(id)).thenReturn(Optional.of(request));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.rejectRequest(id));

        assertEquals("Begäran är redan behandlad (antingen godkänd eller avslagen).", ex.getMessage());
    }

}

