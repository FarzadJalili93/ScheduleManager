package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Service.ShiftService;
import com.example.schedulemanager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/shifts")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private UserService userService;

    // Visa alla skift
    @GetMapping("/all")
    public String viewAllShifts(Model model) {
        List<Shift> shifts = shiftService.getAllShifts();
        model.addAttribute("shifts", shifts);

        // Hämta användaren via Authentication för den inloggade användaren
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            User currentUser = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Användare hittades inte"));
            model.addAttribute("currentUserName", currentUser.getName());
        }

        return "shifts/list";
    }

    // Visa formulär för att skapa ett nytt skift
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("shift", new Shift());
        model.addAttribute("users", userService.getAllUsers()); // För att kunna välja user
        return "shifts/create";
    }

    // Hantera formulärdata för att skapa skift (med separat userId)
    @PostMapping("/create")
    public String createShift(
            @ModelAttribute("shift") Shift shift,
            @RequestParam("userId") Long userId
    ) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Användare med ID " + userId + " hittades inte"));
        shift.setAssignedUser(user);
        shiftService.createShift(shift);
        return "redirect:/shifts/all";
    }

    // Visa formulär för att redigera ett skift
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Shift shift = shiftService.getShiftById(id)
                .orElseThrow(() -> new RuntimeException("Skiftet finns inte"));
        model.addAttribute("shift", shift);
        model.addAttribute("users", userService.getAllUsers()); // Visa valbara användare
        return "shifts/edit";
    }

    // Hantera uppdatering av skift (med separat userId)
    @PostMapping("/edit/{id}")
    public String updateShift(
            @PathVariable Long id,
            @ModelAttribute("shift") Shift updatedShift,
            @RequestParam("userId") Long userId
    ) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Användare med ID " + userId + " hittades inte"));
        updatedShift.setId(id);
        updatedShift.setAssignedUser(user);
        shiftService.updateShift(id, updatedShift);
        return "redirect:/shifts/all";
    }

    // Bekräfta skift
    @GetMapping("/confirm/{id}")
    public String confirmShift(@PathVariable Long id) {
        shiftService.confirmShift(id);
        return "redirect:/shifts/all";
    }

    // Ta bort skift
    @GetMapping("/delete/{id}")
    public String deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return "redirect:/shifts/all";
    }

    // Visa skift per användare
    @GetMapping("/user/{userId}")
    public String viewShiftsByUser(@PathVariable Long userId, Model model) {
        List<Shift> userShifts = shiftService.getShiftsByUserId(userId);
        model.addAttribute("shifts", userShifts);
        return "shifts/list";
    }

    // Visa skift för användare på specifikt datum
    @GetMapping("/user/{userId}/date")
    public String viewShiftsByUserAndDate(
            @PathVariable Long userId,
            @RequestParam("date") String date,
            Model model
    ) {
        LocalDate localDate = LocalDate.parse(date);
        List<Shift> shifts = shiftService.getShiftsByUserIdAndDate(userId, localDate);
        model.addAttribute("shifts", shifts);
        return "shifts/list";
    }

    @GetMapping("/my-shifts")
    public String viewMyShifts(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Om användaren inte är inloggad eller är en anonym användare
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth/login"; // Omdirigera till inloggning om inte inloggad
        }

        // Hämta användaren från Spring Security (det är vanligt att användarnamnet är e-postadressen)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Hämta användaren från databasen med hjälp av e-posten
        User currentUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Användare hittades inte"));

        // Kontrollera om användaren har rollen "ADMIN" genom att kolla på deras "role" objekt
        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());

        if (isAdmin) {
            // Om användaren är admin, omdirigera till sidan för alla skift
            return "redirect:/shifts/all"; // Admins ser alla skift
        }

        // För en vanlig användare (employee), hämta och visa deras egna skift
        List<Shift> myShifts = shiftService.getShiftsByUserId(currentUser.getId());
        model.addAttribute("shifts", myShifts);
        model.addAttribute("currentUserName", currentUser.getName());

        // Visa mina skift-sidan för "employee"
        return "shifts/my-shifts";
    }


}
