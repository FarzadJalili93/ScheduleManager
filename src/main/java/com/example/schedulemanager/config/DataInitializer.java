package com.example.schedulemanager.config;

import com.example.schedulemanager.Entities.Role;
import com.example.schedulemanager.Repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;


@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.findByName("EMPLOYEE").isEmpty()) {
            roleRepository.save(new Role("EMPLOYEE", "Standardanställd"));
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN", "Administratör"));
        }

        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN").get();
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            userRepository.save(admin);
            System.out.println("Första admin skapad: admin@admin.com / admin123");
        }
    }
}

