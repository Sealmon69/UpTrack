package org.uptrack;
import java.util.List;

public interface UserRepository {
    void addUser(User user);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(String username);
}
