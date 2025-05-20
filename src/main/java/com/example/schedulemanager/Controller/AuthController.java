package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.RoleRepository;
import com.example.schedulemanager.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            authService.registerUser(user);
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("error", "Registreringen misslyckades: Konto med Användarnamn/Mailadress finns redan ");
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logoutUser() {
        return "redirect:/auth/logout";
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @GetMapping("/register-admin")
    public String showAdminRegistrationForm(Model model) {
        if (!isCurrentUserAdmin()) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "auth/register-admin";
    }

    @PostMapping("/register-admin")
    public String registerAdmin(@ModelAttribute("user") User user, Model model) {
        if (!isCurrentUserAdmin()) {
            return "redirect:/";
        }

        try {
            authService.registerAdmin(user);
            return "redirect:/shifts/all";
        } catch (Exception e) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Registreringen av admin misslyckades: Konto med Användarnamn/Mailadress finns redan ");
            return "auth/register-admin";
        }
    }
}

