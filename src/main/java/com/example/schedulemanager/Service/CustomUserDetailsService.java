package com.example.schedulemanager.Service;

import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.example.schedulemanager.config.CustomUserDetails; // Se till att du har skapat denna klassen

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Användare inte hittad med e-post: " + email));

        return new CustomUserDetails(user);
    }
}
