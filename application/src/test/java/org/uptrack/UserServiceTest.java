package org.uptrack;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        // System.out umleiten, um Ausgaben zu testen
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        // Systemausgabe zurücksetzen
        System.setOut(originalOut);
    }

    @Test
    void registerUser_shouldAddUserToRepository() {
        // Act
        userService.registerUser("testUser", "password");

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).addUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("testUser", capturedUser.getUsername());
        assertEquals("password", capturedUser.getPassword());
    }

    @Test
    void loginUser_shouldPrintConfirmationMessage() {
        // Act
        userService.loginUser("testUser", "password");

        // Assert
        assertTrue(outputStream.toString().contains("Benutzer testUser erfolgreich angemeldet"));
    }

    @Test
    void logoutUser_shouldPrintConfirmationMessage() {
        // Act
        userService.logoutUser("testUser");

        // Assert
        assertTrue(outputStream.toString().contains("Benutzer testUser erfolgreich abgemeldet"));
    }

    @Test
    void getAllUsers_shouldReturnRepositoryResult() {
        // Arrange
        Admin admin = new Admin("admin", "password");
        when(userRepository.getAllUsers()).thenReturn(Collections.singletonList(admin));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).getUsername());
        verify(userRepository).getAllUsers();
    }

    @Test
    void searchUsers_withMatchingTerm_shouldReturnFilteredList() {
        // Arrange
        Admin admin1 = new Admin("admin", "password");
        Admin admin2 = new Admin("user", "password");
        when(userRepository.getAllUsers()).thenReturn(Arrays.asList(admin1, admin2));

        // Act
        List<User> users = userService.searchUsers("admin");

        // Assert
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).getUsername());
        verify(userRepository).getAllUsers();
    }

    @Test
    void searchUsers_withNonMatchingTerm_shouldReturnEmptyList() {
        // Arrange
        Admin admin = new Admin("admin", "password");
        when(userRepository.getAllUsers()).thenReturn(Collections.singletonList(admin));

        // Act
        List<User> users = userService.searchUsers("nonexistent");

        // Assert
        assertTrue(users.isEmpty());
        verify(userRepository).getAllUsers();
    }

    @Test
    void deleteUser_shouldCallRepositoryDelete() {
        // Act
        userService.deleteUser("testUser");

        // Assert
        verify(userRepository).deleteUser("testUser");
    }

    @Test
    void updateUser_shouldPrintConfirmationMessage() {
        // Act
        userService.updateUser("testUser", "newPassword");

        // Assert
        assertTrue(outputStream.toString().contains("Benutzer testUser wurde aktualisiert"));
    }
}