package org.uptrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository implements UserRepository {
    private Map<String, User> users;

    public Repository() {
        users = new HashMap<>();
        // Initialisiere mit Admin-Benutzer
        users.put("admin", new Admin("admin", "password"));
    }

    @Override
    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    @Override
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void updateUser(User user) {
        if (users.containsKey(user.getUsername())) {
            users.put(user.getUsername(), user);
        }
    }

    @Override
    public void deleteUser(String username) {
        users.remove(username);
    }
}