package com.example.schedulemanager;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.ShiftSwapRequest;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.ShiftRepository;
import com.example.schedulemanager.Repositories.ShiftSwapRequestRepository;
import com.example.schedulemanager.Service.ShiftSwapRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShiftSwapRequestServiceTest {

    private ShiftSwapRequestRepository shiftSwapRequestRepository;
    private ShiftRepository shiftRepository;
    private ShiftSwapRequestService service;

    @BeforeEach
    void setUp() {
        shiftSwapRequestRepository = mock(ShiftSwapRequestRepository.class);
        shiftRepository = mock(ShiftRepository.class);
        service = new ShiftSwapRequestService(shiftSwapRequestRepository, shiftRepository);
    }

    @Test
    void createSwapRequest() {
        ShiftSwapRequest request = new ShiftSwapRequest();
        request.setTargetUser(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.createSwapRequest(request));

        assertEquals("Target user cannot be null", ex.getMessage());
        verifyNoInteractions(shiftSwapRequestRepository);
    }

    @Test
    void getAllRequestsShouldReturnList() {
        ShiftSwapRequest req1 = new ShiftSwapRequest();
        ShiftSwapRequest req2 = new ShiftSwapRequest();

        when(shiftSwapRequestRepository.findAll()).thenReturn(Arrays.asList(req1, req2));

        var result = service.getAllRequests();

        assertEquals(2, result.size());
        verify(shiftSwapRequestRepository).findAll();
    }

    @Test
    void approveRequestShouldUpdateShiftsAndStatus() {
        Long id = 1L;
        User requester = new User();
        User targetUser = new User();
        Shift requestedShift = new Shift();
        requestedShift.setAssignedUser(requester);
        Shift desiredShift = new Shift();
        desiredShift.setAssignedUser(targetUser);

        ShiftSwapRequest request = new ShiftSwapRequest();
        request.setId(id);
        request.setRequester(requester);
        request.setTargetUser(targetUser);
        request.setRequestedShift(requestedShift);
        request.setDesiredShift(desiredShift);

        when(shiftSwapRequestRepository.findById(id)).thenReturn(Optional.of(request));
        when(shiftSwapRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(shiftRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var approved = service.approveRequest(id);

        assertEquals(targetUser, requestedShift.getAssignedUser());
        assertEquals(requester, desiredShift.getAssignedUser());
        assertEquals("APPROVED", approved.getStatus());

        verify(shiftSwapRequestRepository).findById(id);
        verify(shiftSwapRequestRepository).save(request);
        verify(shiftRepository).save(requestedShift);
        verify(shiftRepository).save(desiredShift);
    }

    @Test
    void declineRequestShouldSetStatusToDeclined() {
        Long id = 1L;
        ShiftSwapRequest request = new ShiftSwapRequest();
        request.setId(id);
        request.setStatus("PENDING");

        when(shiftSwapRequestRepository.findById(id)).thenReturn(Optional.of(request));
        when(shiftSwapRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var declined = service.declineRequest(id);

        assertEquals("DECLINED", declined.getStatus());

        verify(shiftSwapRequestRepository).findById(id);
        verify(shiftSwapRequestRepository).save(request);
    }

}

