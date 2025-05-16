package com.example.schedulemanager.Repositories;

import com.example.schedulemanager.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByIdNot(Long id);
}
