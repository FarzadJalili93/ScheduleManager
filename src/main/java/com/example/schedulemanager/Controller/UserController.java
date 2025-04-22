package com.example.schedulemanager.Controller;

import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Hämta alla användare
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);  // 200 OK med användarlistan
    }

    // Hämta användare baserat på ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)  // 200 OK med användardata
                .orElseGet(() -> ResponseEntity.status(404).body(null));  // 404 Not Found
    }

    // Hämta användare baserat på e-post
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)  // 200 OK med användardata
                .orElseGet(() -> ResponseEntity.status(404).body(null));  // 404 Not Found om användaren inte finns
    }

    // Skapa en ny användare
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(201).body(createdUser);  // 201 Created med den skapade användaren
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(null);  // 400 Bad Request vid fel
        }
    }

    // Uppdatera användare
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            updatedUser.setId(id);  // Se till att id sätts rätt för uppdatering
            return ResponseEntity.ok(userService.updateUser(id, updatedUser));  // 200 OK med den uppdaterade användaren
        } else {
            return ResponseEntity.status(404).body(null);  // 404 Not Found om användaren inte finns
        }
    }

    // Radera användare
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            userService.deleteUser(id);  // Försök att ta bort användaren
            return ResponseEntity.noContent().build();  // 204 No Content vid framgång
        } else {
            return ResponseEntity.status(404).build();  // 404 Not Found om användaren inte finns
        }
    }
}
