package com.example.schedulemanager.config;

import com.example.schedulemanager.Entities.Role;
import com.example.schedulemanager.Repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component  // Gör denna klass till en Spring Bean så den kan autowiras
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    // Konstruktor för att injicera RoleRepository
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        // Kontrollera om rollerna finns i databasen
        if (roleRepository.findByName("EMPLOYEE").isEmpty()) {
            // Skapa roll om den inte finns
            roleRepository.save(new Role("EMPLOYEE", "Standardanställd"));
        }
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN", "Administratör"));
        }
    }
}
