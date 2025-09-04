package com.example.my_todo_app.repository;


import com.example.my_todo_app.model.Task;
import com.example.my_todo_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);

    Optional<Task> findByIdAndUser(Long id, User user);
}
