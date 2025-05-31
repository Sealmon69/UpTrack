package org.uptrack;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void registerUser(String username, String password) {
        User newUser = new Admin(username, password); // oder anderen Benutzertyp
        repository.addUser(newUser);
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
        return repository.getAllUsers();
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
        repository.deleteUser(username);
    }

    public void updateUser(String username, String newPassword) {
        // Logik zum Aktualisieren eines Benutzers
        System.out.println("Benutzer " + username + " wurde aktualisiert.");
    }
}