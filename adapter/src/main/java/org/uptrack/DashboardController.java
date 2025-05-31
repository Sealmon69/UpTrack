package org.uptrack;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class DashboardController {

    private final UserControllerService userControllerService;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button userCreate;

    @FXML
    private TableView<UserViewModel> userTableView;

    @FXML
    private TableColumn<UserViewModel, String> usernameColumn;

    @FXML
    private TableColumn<UserViewModel, String> userTypeColumn;

    @FXML
    private TableColumn<UserViewModel, String> statusColumn;

    @FXML
    private TableColumn<UserViewModel, String> actionsColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea notesTextArea;

    public DashboardController(UserControllerService userControllerService) {
        this.userControllerService = userControllerService;
    }

    @FXML
    public void initialize() {
        // Tabellenspalten konfigurieren
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Aktionsspalte mit Buttons befüllen
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Bearbeiten");
            private final Button deleteButton = new Button("Löschen");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                deleteButton.setOnAction(event -> {
                    UserViewModel user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });

        userTableView.setItems(userControllerService.getUserViewModels());

        // Daten laden
        userControllerService.loadUsers();

        // Beispiel: Angenommener eingeloggter Benutzer
        usernameLabel.setText("Angemeldet als: admin");
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        String username = usernameLabel.getText().replace("Angemeldet als: ", "");
        userControllerService.logoutUser(username);

        // UI-Update oder Navigation zum Login-Bildschirm
        statusLabel.setText("Benutzer abgemeldet");
    }

    @FXML
    public void handleSearch(ActionEvent actionEvent) {
        String searchTerm = searchField.getText();
        userControllerService.searchUsers(searchTerm);
        statusLabel.setText("Suche nach: " + searchTerm);
    }

    @FXML
    public void handleNewUser(ActionEvent actionEvent) {
        // In einer echten Anwendung würde hier ein Dialog geöffnet werden
        // Als Beispiel fügen wir einen neuen Benutzer direkt hinzu
        userControllerService.createUser("newUser", "password", "Benutzer");
        statusLabel.setText("Neuer Benutzer wurde erstellt");
    }

    private void handleEditUser(UserViewModel user) {
        // In einer echten Anwendung würde hier ein Dialog geöffnet werden
        statusLabel.setText("Benutzer wird bearbeitet: " + user.getUsername());
    }

    private void handleDeleteUser(UserViewModel user) {
        // In einer echten Anwendung würde hier eine Bestätigung angefordert werden
        userControllerService.deleteUser(user);
        statusLabel.setText("Benutzer wurde gelöscht: " + user.getUsername());
    }

    @FXML
    public void saveNotes() {
        // Code zum Speichern der Notizen
        statusLabel.setText("Notizen gespeichert");
    }
}