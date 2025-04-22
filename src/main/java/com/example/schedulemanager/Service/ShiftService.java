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

    // Hämta alla skift
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    // Hämta skift baserat på ID
    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }

    // Hämta skift för en specifik användare
    public List<Shift> getShiftsByUserId(Long userId) {
        return shiftRepository.findByAssignedUserId(userId);  // Metod som vi definierade i ShiftRepository
    }

    // Hämta skift baserat på datum
    public List<Shift> getShiftsByDate(LocalDate date) {
        return shiftRepository.findByDate(date);  // Metod som vi definierade i ShiftRepository
    }

    // Hämta skift för en specifik användare på ett specifikt datum
    public List<Shift> getShiftsByUserIdAndDate(Long userId, LocalDate date) {
        return shiftRepository.findByAssignedUserIdAndDate(userId, date);  // Metod som vi definierade i ShiftRepository
    }

    // Skapa ett nytt skift
    public Shift createShift(Shift shift) {
        // Kontrollera att sluttiden inte är före starttiden
        if (shift.getEndTime().isBefore(shift.getStartTime())) {
            throw new RuntimeException("Sluttiden kan inte vara före starttiden.");
        }

        // Validera att användaren inte redan har ett skift på det här datumet
        List<Shift> existingShifts = shiftRepository.findByAssignedUserIdAndDate(shift.getAssignedUser().getId(), shift.getDate());
        if (!existingShifts.isEmpty()) {
            throw new RuntimeException("Användaren har redan ett skift på det här datumet.");
        }

        // Spara det nya skiftet
        return shiftRepository.save(shift);
    }

    // Uppdatera ett skift
    public Shift updateShift(Long id, Shift shift) {
        // Hämta det befintliga skiftet
        Optional<Shift> existingShiftOpt = shiftRepository.findById(id);
        if (existingShiftOpt.isEmpty()) {
            throw new RuntimeException("Skift med ID " + id + " finns inte.");
        }

        Shift existingShift = existingShiftOpt.get();

        // Validera att sluttiden inte är före starttiden
        if (shift.getEndTime().isBefore(shift.getStartTime())) {
            throw new RuntimeException("Sluttiden kan inte vara före starttiden.");
        }

        // Om användaren är ändrad, kontrollera om användaren redan har ett skift på det här datumet
        if (shift.getAssignedUser() != null && !shift.getAssignedUser().equals(existingShift.getAssignedUser())) {
            List<Shift> existingShifts = shiftRepository.findByAssignedUserIdAndDate(shift.getAssignedUser().getId(), shift.getDate());
            if (!existingShifts.isEmpty()) {
                throw new RuntimeException("Den nya användaren har redan ett skift på det här datumet.");
            }
        }

        // Uppdatera skiftet med nya värden
        existingShift.setStartTime(shift.getStartTime());
        existingShift.setEndTime(shift.getEndTime());
        existingShift.setConfirmed(shift.isConfirmed());
        existingShift.setAssignedUser(shift.getAssignedUser()); // Om användaren är uppdaterad

        // Spara det uppdaterade skiftet
        return shiftRepository.save(existingShift);
    }

    // Bekräfta ett skift
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

    // Radera ett skift
    public void deleteShift(Long id) {
        Optional<Shift> shift = shiftRepository.findById(id);
        if (shift.isPresent()) {
            shiftRepository.deleteById(id);
        } else {
            throw new RuntimeException("Skift med ID " + id + " finns inte.");
        }
    }
}
