package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.RoleRepository;
import com.example.schedulemanager.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Visa registreringsformulär
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll()); // Lägg till alla roller till modellen
        return "auth/register";
    }


    // Registrera användare
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        authService.registerUser(user);
        return "redirect:/auth/login";  // Omregistrering lyckades, omdirigera till login
    }

    // Visa inloggningsformulär
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/login";  // Här ändrar vi till "auth/login"
    }

  /*  @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") User user, Model model, HttpSession session) {
        User validUser = authService.loginUser(user.getEmail(), user.getPassword());
        if (validUser != null) {
            session.setAttribute("currentUser", validUser); // 👈 Spara användaren i session

            // 👇 Skicka olika användare till olika sidor beroende på roll
            if (validUser.getRole().getName().equalsIgnoreCase("ADMIN")) {
                return "redirect:/shifts/all";
            } else {
                return "redirect:/shifts/my-shifts";
            }
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }
    }*/

    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate(); // Rensa sessionen
        return "redirect:/"; // Omdirigera till index.html
    }



}
