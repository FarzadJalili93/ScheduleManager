package com.example.schedulemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication // Lägg till detta om CustomUserDetailsService är i paketet "Service"
public class ScheduleManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleManagerApplication.class, args);
    }

}
