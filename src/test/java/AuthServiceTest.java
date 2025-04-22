//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.*;
//
//import Entities.User;
//import Repositories.UserRepository;
//import Service.AuthService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//public class AuthServiceTest {
//
//    private AuthService authService;
//    private UserRepository userRepository;
//    private PasswordEncoder passwordEncoder;
//
//    @BeforeEach
//    public void setup() {
//        // Mocka beroenden
//        userRepository = mock(UserRepository.class);
//        passwordEncoder = mock(PasswordEncoder.class);
//
//        // Skapa instans av AuthService med mockade beroenden
//        authService = new AuthService(userRepository, passwordEncoder);
//    }
//
//    @Test
//    public void testRegisterUser() {
//        User user = new User();
//        user.setEmail("test@example.com");
//        user.setPassword("password123");
//
//        // Mocka lösenordskryptering
//        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
//
//        // Mocka att spara användaren
//        when(userRepository.save(user)).thenReturn(user);
//
//        User savedUser = authService.registerUser(user);
//
//        // Verifiera att lösenordet har blivit krypterat
//        assertEquals("encodedPassword", savedUser.getPassword());
//
//        // Verifiera att användaren sparades
//        verify(userRepository).save(user);
//    }
//
//    @Test
//    public void testLoginUser() {
//        User user = new User();
//        user.setEmail("test@example.com");
//        user.setPassword("password123");
//
//        // Mocka att användaren finns i databasen
//        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
//
//        // Mocka att lösenordet är korrekt
//        when(passwordEncoder.matches(user.getPassword(), user.getPassword())).thenReturn(true);
//
//        // Logga in användaren
//        User loggedInUser = authService.loginUser(user.getEmail(), user.getPassword());
//
//        // Verifiera att vi får rätt användare tillbaka
//        assertEquals(user.getEmail(), loggedInUser.getEmail());
//    }
//}