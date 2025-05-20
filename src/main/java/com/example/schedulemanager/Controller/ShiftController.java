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

    @GetMapping("/all")
    public String viewAllShifts(@RequestParam(required = false) Long userId, Model model) {
        List<Shift> shifts;

        if (userId != null) {
            shifts = shiftService.getShiftsByUserId(userId);
            model.addAttribute("selectedUserId", userId);
        } else {
            shifts = shiftService.getAllShifts();
        }

        List<User> users = userService.getAllUsers();
        model.addAttribute("shifts", shifts);
        model.addAttribute("users", users);

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


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("shift", new Shift());
        model.addAttribute("users", userService.getAllUsers());
        return "shifts/create";
    }

    @PostMapping("/create")
    public String createShift(
            @ModelAttribute("shift") Shift shift,
            @RequestParam("userId") Long userId,
            Model model
    ) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Användare med ID " + userId + " hittades inte"));
        shift.setAssignedUser(user);

        try {
            shiftService.createShift(shift);
            return "redirect:/shifts/all";
        } catch (RuntimeException e) {
            model.addAttribute("shift", shift);
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("errorMessage", e.getMessage());
            return "shifts/create";
        }
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Shift shift = shiftService.getShiftById(id)
                .orElseThrow(() -> new RuntimeException("Skiftet finns inte"));
        model.addAttribute("shift", shift);
        model.addAttribute("users", userService.getAllUsers());
        return "shifts/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateShift(
            @PathVariable Long id,
            @ModelAttribute("shift") Shift updatedShift,
            @RequestParam("userId") Long userId,
            Model model
    ) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Användare med ID " + userId + " hittades inte"));

            updatedShift.setId(id);
            updatedShift.setAssignedUser(user);

            shiftService.updateShift(id, updatedShift);
            return "redirect:/shifts/all";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("shift", updatedShift);
            model.addAttribute("users", userService.getAllUsers());
            return "shifts/edit";
        }
    }


    @GetMapping("/confirm/{id}")
    public String confirmShift(@PathVariable Long id) {
        shiftService.confirmShift(id);
        return "redirect:/shifts/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return "redirect:/shifts/all";
    }

    @GetMapping("/user/{userId}")
    public String viewShiftsByUser(@PathVariable Long userId, Model model) {
        List<Shift> userShifts = shiftService.getShiftsByUserId(userId);
        model.addAttribute("shifts", userShifts);
        return "shifts/list";
    }

    @GetMapping("/user/{userId}/date")
    public String viewShiftsByUserAndDate(
            @PathVariable Long userId,
            @RequestParam("date") String dateString,
            Model model
    ) {
        LocalDate date = LocalDate.parse(dateString);

        List<Shift> shifts = shiftService.getShiftsByUserId(userId);
        List<Shift> filteredShifts = shifts.stream()
                .filter(shift -> shift.getDate().equals(date))
                .toList();

        model.addAttribute("shifts", filteredShifts);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedUserId", userId);

        return "shifts/list";
    }

    @GetMapping("/my-shifts")
    public String viewMyShifts(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        User currentUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Användare hittades inte"));

        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());

        if (isAdmin) {
            return "redirect:/shifts/all";
        }

        List<Shift> myShifts = shiftService.getShiftsByUserId(currentUser.getId());
        model.addAttribute("shifts", myShifts);
        model.addAttribute("currentUserName", currentUser.getName());

        return "shifts/my-shifts";
    }


}
