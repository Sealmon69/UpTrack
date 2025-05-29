package org.uptrack;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void registerUser(String username, String password) {
        // Validierung und Logik zur Benutzerregistrierung
        System.out.println("Benutzer " + username + " erfolgreich registriert.");
    }

    public void loginUser(String username, String password) {
        // Authentifizierungslogik
        System.out.println("Benutzer " + username + " erfolgreich angemeldet.");
    }

    public void logoutUser(String username) {
        // Abmeldelogik
        System.out.println("Benutzer " + username + " erfolgreich abgemeldet.");
    }

    public List<User> getAllUsers() {
        // In einer realen Implementierung würden wir Benutzer aus dem Repository abrufen
        List<User> users = new ArrayList<>();
        users.add(new Admin("admin", "password"));
        // Weitere Benutzer aus dem Repository hinzufügen
        return users;
    }

    public List<User> searchUsers(String searchTerm) {
        // Suchlogik implementieren
        List<User> allUsers = getAllUsers();
        List<User> filteredUsers = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getUsername().contains(searchTerm)) {
                filteredUsers.add(user);
            }
        }

        return filteredUsers;
    }

    public void deleteUser(String username) {
        // Logik zum Löschen eines Benutzers
        System.out.println("Benutzer " + username + " wurde gelöscht.");
    }

    public void updateUser(String username, String newPassword) {
        // Logik zum Aktualisieren eines Benutzers
        System.out.println("Benutzer " + username + " wurde aktualisiert.");
    }
}