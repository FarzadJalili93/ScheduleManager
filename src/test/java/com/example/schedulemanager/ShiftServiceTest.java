package com.example.schedulemanager;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.ShiftRepository;
import com.example.schedulemanager.Service.ShiftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    private Shift shift;
    private User user;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        date = LocalDate.of(2025, 5, 15);

        shift = new Shift();
        shift.setId(1L);
        shift.setAssignedUser(user);
        shift.setDate(date);
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(17, 0));
        shift.setConfirmed(false);
    }

    @Test
    void createShiftSuccess() {
        when(shiftRepository.findByAssignedUserIdAndDate(1L, date)).thenReturn(Collections.emptyList());
        when(shiftRepository.save(shift)).thenReturn(shift);

        Shift result = shiftService.createShift(shift);

        assertEquals(shift, result);
        verify(shiftRepository).findByAssignedUserIdAndDate(1L, date);
        verify(shiftRepository).save(shift);
    }

    @Test
    void createShiftThrowsWhenEndTimeBeforeStartTime() {
        shift.setEndTime(LocalTime.of(8, 0));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> shiftService.createShift(shift));
        assertEquals("Sluttiden kan inte vara före starttiden.", ex.getMessage());

        verifyNoInteractions(shiftRepository);
    }

    @Test
    void createShiftThrowsWhenShiftAlreadyExists() {
        when(shiftRepository.findByAssignedUserIdAndDate(1L, date)).thenReturn(List.of(shift));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> shiftService.createShift(shift));
        assertEquals("Användaren har redan ett skift på det här datumet.", ex.getMessage());

        verify(shiftRepository).findByAssignedUserIdAndDate(1L, date);
        verify(shiftRepository, never()).save(any());
    }

    @Test
    void updateShiftSuccess() {
        Shift updatedShift = new Shift();
        updatedShift.setStartTime(LocalTime.of(10, 0));
        updatedShift.setEndTime(LocalTime.of(18, 0));
        updatedShift.setConfirmed(true);
        updatedShift.setAssignedUser(user);
        updatedShift.setDate(date);

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(i -> i.getArgument(0));

        Shift result = shiftService.updateShift(1L, updatedShift);

        assertEquals(updatedShift.getStartTime(), result.getStartTime());
        assertEquals(updatedShift.getEndTime(), result.getEndTime());
        assertTrue(result.isConfirmed());
        assertEquals(updatedShift.getAssignedUser(), result.getAssignedUser());
    }

    @Test
    void updateShiftThrowsWhenShiftNotFound() {
        when(shiftRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> shiftService.updateShift(1L, shift));
        assertEquals("Skift med ID 1 finns inte.", ex.getMessage());
    }

    @Test
    void updateShiftThrowsWhenEndTimeBeforeStartTime() {
        Shift updatedShift = new Shift();
        updatedShift.setStartTime(LocalTime.of(10, 0));
        updatedShift.setEndTime(LocalTime.of(9, 0)); // ogiltig sluttid
        updatedShift.setAssignedUser(user);
        updatedShift.setDate(date);

        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> shiftService.updateShift(1L, updatedShift));
        assertEquals("Sluttiden kan inte vara före starttiden.", ex.getMessage());
    }

    @Test
    void updateShiftThrowsWhenNewUserAlreadyHasShift() {
        // Arrange
        User newUser = new User();
        newUser.setId(2L);

        Shift updatedShift = new Shift();
        updatedShift.setStartTime(LocalTime.of(10, 0));
        updatedShift.setEndTime(LocalTime.of(18, 0));
        updatedShift.setAssignedUser(newUser);
        updatedShift.setDate(date);

        // Det skift som redan ligger i systemet (ska kastas fel för att användaren redan har ett skift)
        Shift existingShift = new Shift();
        existingShift.setId(99L);  // Viktigt! Annars kraschar equals() när id är null

        // Mocka vad shiftRepository ska returnera
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.findByAssignedUserIdAndDate(2L, date)).thenReturn(List.of(existingShift));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            shiftService.updateShift(1L, updatedShift);
        });

        // Kontrollera felmeddelande
        assertEquals("Användaren har redan ett annat skift på det här datumet.", ex.getMessage());
    }


    @Test
    void confirmShiftSuccess() {
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(i -> i.getArgument(0));

        Shift result = shiftService.confirmShift(1L);

        assertTrue(result.isConfirmed());
        verify(shiftRepository).findById(1L);
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void confirmShiftThrows_WhenShiftNotFound() {
        when(shiftRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> shiftService.confirmShift(1L));
        assertEquals("Skift med ID 1 finns inte.", ex.getMessage());
    }

    @Test
    void deleteShiftSuccess() {
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));

        shiftService.deleteShift(1L);

        verify(shiftRepository).deleteById(1L);
    }

    @Test
    void deleteShiftThrowsWhenShiftNotFound() {
        when(shiftRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> shiftService.deleteShift(1L));
        assertEquals("Skift med ID 1 finns inte.", ex.getMessage());
    }
}
