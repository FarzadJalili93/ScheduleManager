package com.example.schedulemanager.Repositories;

import com.example.schedulemanager.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Hämta användare baserat på e-post
    Optional<User> findByEmail(String email);

    // Hämta alla användare som inte har det angivna ID:t
    List<User> findByIdNot(Long id);
}
