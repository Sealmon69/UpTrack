package org.uptrack;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserViewModel {
    private final StringProperty username;
    private final StringProperty userType;
    private final StringProperty status;

    public UserViewModel(String username, String userType, String status) {
        this.username = new SimpleStringProperty(username);
        this.userType = new SimpleStringProperty(userType);
        this.status = new SimpleStringProperty(status);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getUserType() {
        return userType.get();
    }

    public StringProperty userTypeProperty() {
        return userType;
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }
}