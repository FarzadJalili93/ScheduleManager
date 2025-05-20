package com.example.schedulemanager;

import com.example.schedulemanager.Entities.Role;
import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.RoleRepository;
import com.example.schedulemanager.Repositories.UserRepository;
import com.example.schedulemanager.Service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("USER");

        user = new User();
        user.setId(1L);
        user.setName("testuser");
        user.setPassword("plaintextpassword");
        user.setRole(role);
    }

    @Test
    void registerUserShouldEncodePasswordAndSaveUser() {
        when(roleRepository.findByName("employee")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plaintextpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User registeredUser = authService.registerUser(user);

        verify(passwordEncoder).encode("plaintextpassword");
        verify(roleRepository).findByName("employee");
        verify(userRepository).save(any(User.class));

        assertEquals("encodedpassword", registeredUser.getPassword());
        assertEquals(role, registeredUser.getRole());
    }

    @Test
    void registerUserShouldThrowExceptionWhenRoleNotFound() {
        when(roleRepository.findByName("employee")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(user);
        });

        assertEquals("Role not found", exception.getMessage());
        verify(roleRepository).findByName("employee");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }
}
