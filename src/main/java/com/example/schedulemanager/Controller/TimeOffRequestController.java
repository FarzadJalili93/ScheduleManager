package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.TimeOffRequest;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Enum.ApprovalStatus;
import com.example.schedulemanager.Service.TimeOffRequestService;
import com.example.schedulemanager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/timeoff")
public class TimeOffRequestController {

    private final TimeOffRequestService timeOffRequestService;
    private final UserService userService;

    @Autowired
    public TimeOffRequestController(TimeOffRequestService timeOffRequestService, UserService userService) {
        this.timeOffRequestService = timeOffRequestService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Användare hittades inte"));
        }
        return null;
    }

    @GetMapping("/list")
    public String listRequests(@RequestParam(name = "userId", required = false) Long userId, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null && "ADMIN".equals(currentUser.getRole().getName())) {
            List<TimeOffRequest> requests;
            if (userId != null) {
                requests = timeOffRequestService.getRequestsByUserId(userId);
            } else {
                requests = timeOffRequestService.getAllRequests();
            }

            List<User> users = userService.getAllUsers();

            model.addAttribute("requests", requests);
            model.addAttribute("users", users);
            model.addAttribute("selectedUserId", userId);

            return "timeoff/list";
        }
        return "redirect:/timeoff/my";
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("timeOffRequest", new TimeOffRequest());
        return "timeoff/create";
    }

    @PostMapping("/create")
    public String createRequest(@ModelAttribute("timeOffRequest") TimeOffRequest request, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            request.setUser(currentUser);
            request.setApprovalStatus(ApprovalStatus.PENDING);
            try {
                timeOffRequestService.createRequest(request);
                return "redirect:/timeoff/my";
            } catch (RuntimeException e) {
                model.addAttribute("errorMessage", e.getMessage());
                model.addAttribute("timeOffRequest", request);
                return "timeoff/create";
            }
        }
        return "redirect:/login";  
    }

    @GetMapping("/my")
    public String viewMyRequests(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            List<TimeOffRequest> myRequests = timeOffRequestService.getRequestsByUserId(currentUser.getId());
            model.addAttribute("requests", myRequests);
        }
        return "timeoff/my";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null && "ADMIN".equals(currentUser.getRole().getName())) {
            TimeOffRequest request = timeOffRequestService.getRequestById(id).orElse(null);
            if (request != null) {
                model.addAttribute("timeOffRequest", request);
                model.addAttribute("currentUser", currentUser);
                return "timeoff/edit";
            }
        }
        return "redirect:/timeoff/my";
    }

    @PostMapping("/edit/{id}")
    public String updateRequest(@PathVariable Long id, @ModelAttribute("timeOffRequest") TimeOffRequest updatedRequest, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName())) {
            TimeOffRequest existingRequest = timeOffRequestService.getRequestById(id).orElse(null);
            if (existingRequest != null) {
                updatedRequest.setId(id);
                updatedRequest.setUser(existingRequest.getUser());
                try {
                    timeOffRequestService.updateRequest(id, updatedRequest);
                    return "redirect:/timeoff/list";
                } catch (RuntimeException e) {
                    model.addAttribute("errorMessage", e.getMessage());
                    model.addAttribute("timeOffRequest", updatedRequest);
                    return "timeoff/edit";
                }
            }
        }
        return "redirect:/timeoff/my";
    }

    @GetMapping("/delete/{id}")
    public String deleteRequest(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        TimeOffRequest request = timeOffRequestService.getRequestById(id).orElse(null);
        if (request != null && currentUser != null &&
                request.getUser().getId().equals(currentUser.getId())) {
            timeOffRequestService.deleteRequest(id);
        }
        return "redirect:/timeoff/my";
    }

    @PostMapping("/{id}/status")
    public String updateApprovalStatus(@PathVariable Long id, @RequestParam("status") ApprovalStatus status) {
        if (status == ApprovalStatus.APPROVED) {
            timeOffRequestService.approveRequest(id);
        } else if (status == ApprovalStatus.REJECTED) {
            timeOffRequestService.rejectRequest(id);
        }
        return "redirect:/timeoff/list";
    }
}
