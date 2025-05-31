package org.uptrack;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void testConstructor() {
        // Arrange & Act
        Admin admin = new Admin("admin", "password123");

        // Assert
        assertEquals("admin", admin.getUsername());
        assertEquals("password123", admin.getPassword());
    }

    @Test
    void testSetUsername() {
        // Arrange
        Admin admin = new Admin("admin", "password123");

        // Act
        admin.setUsername("newAdmin");

        // Assert
        assertEquals("newAdmin", admin.getUsername());
    }

    @Test
    void testSetPassword() {
        // Arrange
        Admin admin = new Admin("admin", "password123");

        // Act
        admin.setPassword("newPassword");

        // Assert
        assertEquals("newPassword", admin.getPassword());
    }

    @Test
    void testUserInterfaceImplementation() {
        // Arrange & Act
        User user = new Admin("admin", "password123");

        // Assert
        assertEquals("admin", user.getUsername());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testUserInterfaceSetters() {
        // Arrange
        User user = new Admin("admin", "password123");

        // Act
        user.setUsername("newUsername");
        user.setPassword("newPassword");

        // Assert
        assertEquals("newUsername", user.getUsername());
        assertEquals("newPassword", user.getPassword());
    }
}