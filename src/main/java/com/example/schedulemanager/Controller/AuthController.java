package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User savedUser = authService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }

   @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        User validUser = authService.loginUser(user.getEmail(), user.getPassword());
        if (validUser != null) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
}