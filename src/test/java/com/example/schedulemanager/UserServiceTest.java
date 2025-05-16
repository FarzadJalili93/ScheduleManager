package com.example.schedulemanager;

import com.example.schedulemanager.Entities.User;
import com.example.schedulemanager.Repositories.UserRepository;
import com.example.schedulemanager.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new UserService(userRepository);
    }


    @Test
    void getAllUsersShouldReturnList() {
        User user1 = new User();
        User user2 = new User();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = service.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserByIdShouldReturnUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> found = service.getUserById(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByEmailShouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> found = service.getUserByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void createUserWhenEmailExistsShouldThrow() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createUser(user));

        assertEquals("Användare med denna e-postadress finns redan", ex.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserWhenEmailNotExistsShouldSave() {
        User user = new User();
        user.setEmail("new@example.com");
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User saved = service.createUser(user);

        assertEquals("new@example.com", saved.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserWhenExistsShouldSave() {
        User existingUser = new User();
        existingUser.setId(1L);
        User updatedUser = new User();
        updatedUser.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        User saved = service.updateUser(1L, updatedUser);

        assertEquals("updated@example.com", saved.getEmail());
        assertEquals(1L, saved.getId());
        verify(userRepository).save(updatedUser);
    }

    @Test
    void updateUserWhenNotExistsShouldThrow() {
        User updatedUser = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateUser(1L, updatedUser));

        assertEquals("Användare med ID 1 finns inte", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserWhenExistsShouldDelete() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        service.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserWhenNotExistsShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.deleteUser(1L));

        assertEquals("Användare med ID 1 finns inte", ex.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void updateUserPasswordWhenExistsShouldUpdatePassword() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPassword("oldPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User updated = service.updateUserPassword(1L, "newPass");

        assertEquals("newPass", updated.getPassword());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserPasswordWhenNotExistsShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateUserPassword(1L, "newPass"));

        assertEquals("Användare med ID 1 finns inte", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}

