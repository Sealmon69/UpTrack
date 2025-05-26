package org.uptrack;

public interface User {
    String getUsername();
    void setUsername(String username);
    String getPassword();
    void setPassword(String password);
    void login();

}
