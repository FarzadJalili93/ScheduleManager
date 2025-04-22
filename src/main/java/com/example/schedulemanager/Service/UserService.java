package com.example.schedulemanager.Service;

import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Hämta alla användare
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Hämta användare baserat på ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Hämta användare baserat på e-post
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Skapa en ny användare
    public User createUser(User user) {
        // Kolla om e-postadressen redan finns
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Användare med denna e-postadress finns redan");
        }
        return userRepository.save(user);
    }

    // Uppdatera användare
    public User updateUser(Long id, User updatedUser) {
        // Kontrollera om användaren finns innan uppdatering
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            updatedUser.setId(id); // Sätt ID för att uppdatera det befintliga användarkontot
            return userRepository.save(updatedUser);
        } else {
            throw new RuntimeException("Användare med ID " + id + " finns inte");
        }
    }

    // Ta bort användare
    public void deleteUser(Long id) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("Användare med ID " + id + " finns inte");
        }
    }

    // Uppdatera användarens lösenord
    public User updateUserPassword(Long id, String newPassword) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setPassword(newPassword); // Här kan du lägga till lösenordshantering (t.ex. hashning)
            return userRepository.save(user);
        } else {
            throw new RuntimeException("Användare med ID " + id + " finns inte");
        }
    }
}
