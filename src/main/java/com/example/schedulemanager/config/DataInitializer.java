package com.example.schedulemanager.config;

import com.example.schedulemanager.Entities.Role;
import com.example.schedulemanager.Repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;


    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.findByName("EMPLOYEE").isEmpty()) {
            roleRepository.save(new Role("EMPLOYEE", "Standardanställd"));
        }
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN", "Administratör"));
        }
    }
}
