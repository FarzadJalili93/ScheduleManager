package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.Shift;
import com.example.schedulemanager.Entities.ShiftSwapRequest;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Service.ShiftService;
import com.example.schedulemanager.Service.ShiftSwapRequestService;
import com.example.schedulemanager.Service.UserService;
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

    public ShiftSwapRequestController(ShiftSwapRequestService shiftSwapRequestService,
                                      UserService userService,
                                      ShiftService shiftService) {
        this.shiftSwapRequestService = shiftSwapRequestService;
        this.userService = userService;
        this.shiftService = shiftService;
    }

    // Visa alla bytesförfrågningar
    @GetMapping
    public String listRequests(Model model) {
        List<ShiftSwapRequest> requests = shiftSwapRequestService.getAllRequests();
        model.addAttribute("requests", requests);
        return "swaprequests/list";
    }

    // Visa formulär för att skapa ny bytesförfrågan
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User loggedInUser = getLoggedInUser();
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        List<User> users = userService.getAllUsersExcept(loggedInUser.getId());
        List<Shift> userShifts = shiftService.getShiftsByUserId(loggedInUser.getId());
        List<Shift> otherShifts = shiftService.getShiftsByOtherUsers(loggedInUser.getId());

        if (userShifts.isEmpty()) {
            throw new RuntimeException("Den inloggade användaren har inga skift tillgängliga.");
        }

        model.addAttribute("swapRequest", new ShiftSwapRequest());
        model.addAttribute("users", users);
        model.addAttribute("availableShifts", userShifts);
        model.addAttribute("otherShifts", otherShifts);

        return "swaprequests/create";
    }

    // Skicka ny bytesförfrågan
    @PostMapping("/create")
    public String createRequest(@ModelAttribute("swapRequest") ShiftSwapRequest request) {
        User loggedInUser = getLoggedInUser();
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        if (request.getRequestedShift() == null || request.getDesiredShift() == null) {
            throw new RuntimeException("Välj både ett skift att byta bort och ett skift att få.");
        }

        request.setRequester(loggedInUser);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus("PENDING");

        shiftSwapRequestService.createSwapRequest(request);
        return "redirect:/swap-requests";
    }

    // Godkänn förfrågan
    @GetMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id) {
        shiftSwapRequestService.approveRequest(id);
        return "redirect:/swap-requests";
    }

    // Avvisa förfrågan
    @GetMapping("/decline/{id}")
    public String declineRequest(@PathVariable Long id) {
        shiftSwapRequestService.declineRequest(id);
        return "redirect:/swap-requests";
    }

    // Ta bort förfrågan
    @GetMapping("/delete/{id}")
    public String deleteRequest(@PathVariable Long id) {
        shiftSwapRequestService.deleteRequest(id);
        return "redirect:/swap-requests";
    }

    // 🔒 Hjälpmetod för att hämta inloggad användare via Spring Security
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
