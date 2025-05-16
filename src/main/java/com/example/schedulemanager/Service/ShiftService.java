package com.example.schedulemanager.Service;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Repositories.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    public List<Shift> getShiftsByUserId(Long userId) {
        return shiftRepository.findByAssignedUserId(userId);
    }

    public List<Shift> getShiftsByDate(LocalDate date) {
        return shiftRepository.findByDate(date);
    }

    public List<Shift> getShiftsByUserIdAndDate(Long userId, LocalDate date) {
        return shiftRepository.findByAssignedUserIdAndDate(userId, date);
    }

    public List<Shift> getShiftsByOtherUsers(Long userId) {
        return shiftRepository.findByAssignedUserIdNot(userId);
    }


    public Shift createShift(Shift shift) {
        if (shift.getEndTime().isBefore(shift.getStartTime())) {
            throw new RuntimeException("Sluttiden kan inte vara före starttiden.");
        }

        List<Shift> existingShifts = shiftRepository.findByAssignedUserIdAndDate(shift.getAssignedUser().getId(), shift.getDate());
        if (!existingShifts.isEmpty()) {
            throw new RuntimeException("Användaren har redan ett skift på det här datumet.");
        }

        return shiftRepository.save(shift);
    }

    public Shift updateShift(Long id, Shift shift) {
        Optional<Shift> existingShiftOpt = shiftRepository.findById(id);
        if (existingShiftOpt.isEmpty()) {
            throw new RuntimeException("Skift med ID " + id + " finns inte.");
        }

        Shift existingShift = existingShiftOpt.get();

        if (shift.getEndTime().isBefore(shift.getStartTime())) {
            throw new RuntimeException("Sluttiden kan inte vara före starttiden.");
        }

        if (shift.getAssignedUser() != null && !shift.getAssignedUser().equals(existingShift.getAssignedUser())) {
            List<Shift> existingShifts = shiftRepository.findByAssignedUserIdAndDate(shift.getAssignedUser().getId(), shift.getDate());
            if (!existingShifts.isEmpty()) {
                throw new RuntimeException("Den nya användaren har redan ett skift på det här datumet.");
            }
        }

        existingShift.setStartTime(shift.getStartTime());
        existingShift.setEndTime(shift.getEndTime());
        existingShift.setConfirmed(shift.isConfirmed());
        existingShift.setAssignedUser(shift.getAssignedUser());

        return shiftRepository.save(existingShift);
    }

    public Shift confirmShift(Long id) {
        Optional<Shift> shift = shiftRepository.findById(id);
        if (shift.isPresent()) {
            Shift existingShift = shift.get();
            existingShift.setConfirmed(true);
            return shiftRepository.save(existingShift);
        } else {
            throw new RuntimeException("Skift med ID " + id + " finns inte.");
        }
    }

    public void deleteShift(Long id) {
        Optional<Shift> shift = shiftRepository.findById(id);
        if (shift.isPresent()) {
            shiftRepository.deleteById(id);
        } else {
            throw new RuntimeException("Skift med ID " + id + " finns inte.");
        }
    }
}
