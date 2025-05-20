package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.ShiftSwapRequest;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Service.ShiftService;
import com.example.schedulemanager.Service.ShiftSwapRequestService;
import com.example.schedulemanager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/swap-requests")
public class ShiftSwapRequestController {

    private final ShiftSwapRequestService shiftSwapRequestService;
    private final UserService userService;
    private final ShiftService shiftService;

    @Autowired
    public ShiftSwapRequestController(ShiftSwapRequestService shiftSwapRequestService,
                                      UserService userService,
                                      ShiftService shiftService) {
        this.shiftSwapRequestService = shiftSwapRequestService;
        this.userService = userService;
        this.shiftService = shiftService;
    }

    @GetMapping
    public String listRequests(@RequestParam(required = false) Long userId, Model model) {
        User loggedInUser = getLoggedInUser();

        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        List<ShiftSwapRequest> requests;

        if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole().getName())) {
            if (userId != null) {
                requests = shiftSwapRequestService.getRequestsByRequesterId(userId);
                model.addAttribute("selectedUserId", userId);
            } else {
                requests = shiftSwapRequestService.getAllRequests();
            }
        } else {
            requests = shiftSwapRequestService.getRequestsByRequesterId(loggedInUser.getId());
        }

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("requests", requests);

        return "swaprequests/list";
    }



    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User loggedInUser = getLoggedInUser();
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        try {
            List<User> users = userService.getAllUsersExcept(loggedInUser.getId());
            List<Shift> userShifts = shiftService.getShiftsByUserId(loggedInUser.getId());
            List<Shift> otherShifts = shiftService.getShiftsByOtherUsers(loggedInUser.getId());

            if (userShifts.isEmpty()) {
                model.addAttribute("errorMessage", "Du har inga skift att byta bort.");
            }

            model.addAttribute("swapRequest", new ShiftSwapRequest());
            model.addAttribute("users", users);
            model.addAttribute("availableShifts", userShifts);
            model.addAttribute("otherShifts", otherShifts);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ett fel inträffade: " + e.getMessage());
        }

        return "swaprequests/create";
    }

    @PostMapping("/create")
    public String createRequest(@ModelAttribute("swapRequest") ShiftSwapRequest request, Model model) {
        User loggedInUser = getLoggedInUser();
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        try {
            if (request.getRequestedShift() == null || request.getDesiredShift() == null) {
                model.addAttribute("errorMessage", "Välj både ett skift att byta bort och ett skift att få.");
                return "swaprequests/create";
            }

            request.setRequester(loggedInUser);
            request.setRequestDate(LocalDateTime.now());
            request.setStatus("PENDING");

            shiftSwapRequestService.createSwapRequest(request);
            return "redirect:/swap-requests";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ett fel inträffade: " + e.getMessage());
            return "swaprequests/create";
        }
    }

    @GetMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id) {
        shiftSwapRequestService.approveRequest(id);
        return "redirect:/swap-requests";
    }

    @GetMapping("/decline/{id}")
    public String declineRequest(@PathVariable Long id) {
        shiftSwapRequestService.declineRequest(id);
        return "redirect:/swap-requests";
    }

    @GetMapping("/delete/{id}")
    public String deleteRequest(@PathVariable Long id) {
        shiftSwapRequestService.deleteRequest(id);
        return "redirect:/swap-requests";
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return userService.getUserByEmail(email).orElse(null);
        }
        return null;
    }
}
