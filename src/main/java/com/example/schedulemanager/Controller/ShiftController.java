package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    // Hämta alla skift
    @GetMapping
    public ResponseEntity<List<Shift>> getAllShifts() {
        List<Shift> shifts = shiftService.getAllShifts();
        return ResponseEntity.ok(shifts);  // 200 OK med listan av skift
    }

    // Hämta skift baserat på ID
    @GetMapping("/{id}")
    public ResponseEntity<Shift> getShiftById(@PathVariable Long id) {
        return shiftService.getShiftById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(null));  // 404 Not Found om inget skift hittas
    }

    // Hämta skift för en användare
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Shift>> getShiftsByUserId(@PathVariable Long userId) {
        List<Shift> shifts = shiftService.getShiftsByUserId(userId);
        return ResponseEntity.ok(shifts);  // 200 OK med listan av skift för användaren
    }

    // Hämta skift för en användare på ett specifikt datum
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<List<Shift>> getShiftsByUserIdAndDate(@PathVariable Long userId, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);  // Omvandla datumsträngen till LocalDate
        List<Shift> shifts = shiftService.getShiftsByUserIdAndDate(userId, localDate);
        return ResponseEntity.ok(shifts);  // 200 OK med listan av skift för användaren på det specifika datumet
    }

    // Skapa ett nytt skift
    @PostMapping
    public ResponseEntity<Shift> createShift(@RequestBody Shift shift) {
        try {
            Shift createdShift = shiftService.createShift(shift);
            return ResponseEntity.status(201).body(createdShift);  // 201 Created
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(null);  // 400 Bad Request om något går fel
        }
    }

    // Uppdatera ett skift
    @PutMapping("/{id}")
    public ResponseEntity<Shift> updateShift(@PathVariable Long id, @RequestBody Shift updatedShift) {
        updatedShift.setId(id);  // Se till att ID sätts för uppdatering
        Shift shift = shiftService.updateShift(id, updatedShift);
        return ResponseEntity.ok(shift);  // 200 OK med den uppdaterade skiftinformationen
    }

    // Bekräfta ett skift
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Shift> confirmShift(@PathVariable Long id) {
        try {
            Shift confirmedShift = shiftService.confirmShift(id);
            return ResponseEntity.ok(confirmedShift);  // 200 OK med det bekräftade skiftet
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);  // 404 Not Found om skiftet inte finns
        }
    }

    // Radera ett skift
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        try {
            shiftService.deleteShift(id);
            return ResponseEntity.noContent().build();  // 204 No Content vid framgång
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();  // 404 Not Found om skiftet inte finns
        }
    }
}
