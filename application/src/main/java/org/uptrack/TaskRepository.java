package org.uptrack;

import java.util.List;

public interface TaskRepository {
    List<Tasks> getAllTasks();
    void addTask(Tasks task);
    void updateTask(Tasks task);
    void deleteTask(String title);
}