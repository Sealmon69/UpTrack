package org.uptrack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserControllerService {
    private final UserService userService;
    private final ObservableList<UserViewModel> userViewModels = FXCollections.observableArrayList();

    public UserControllerService() {
        this.userService = new UserService(new Repository());
    }

    public ObservableList<UserViewModel> getUserViewModels() {
        return userViewModels;
    }

    public void loadUsers() {
        // Hier würden wir normalerweise die Benutzer vom UserService laden
        // und in UserViewModels umwandeln
        userViewModels.clear();
        userViewModels.add(new UserViewModel("admin", "Administrator", "Aktiv"));
        userViewModels.add(new UserViewModel("user1", "Benutzer", "Aktiv"));
        userViewModels.add(new UserViewModel("user2", "Benutzer", "Inaktiv"));
    }

    public void searchUsers(String searchTerm) {
        // Hier würden wir die Suche an den UserService delegieren
        System.out.println("Suche nach Benutzern mit Begriff: " + searchTerm);
    }

    public void createUser(String username, String password, String userType) {
        userService.registerUser(username, password);
        userViewModels.add(new UserViewModel(username, userType, "Aktiv"));
    }

    public void editUser(UserViewModel userViewModel, String newUsername, String newUserType, String newStatus) {
        // Hier würden wir die Bearbeitung an den UserService delegieren
        System.out.println("Benutzer bearbeitet: " + userViewModel.getUsername());

        // UI aktualisieren
        int index = userViewModels.indexOf(userViewModel);
        userViewModels.set(index, new UserViewModel(newUsername, newUserType, newStatus));
    }

    public void deleteUser(UserViewModel userViewModel) {
        // Hier würden wir das Löschen an den UserService delegieren
        System.out.println("Benutzer gelöscht: " + userViewModel.getUsername());

        // Aus der Ansicht entfernen
        userViewModels.remove(userViewModel);
    }

    public void logoutUser(String username) {
        userService.logoutUser(username);
    }
}