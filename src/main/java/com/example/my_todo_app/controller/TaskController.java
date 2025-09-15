package com.example.my_todo_app.controller;

import com.example.my_todo_app.model.Task;
import com.example.my_todo_app.model.User;
import com.example.my_todo_app.repository.TaskRepository;
import com.example.my_todo_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

@Controller
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("現在のユーザーが見つかりません"));
    }

    @GetMapping("/")
    public String listTasks(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("tasks", taskRepository.findWithUserByUser(currentUser));
        return "index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("task", new Task());
        return "create";
    }

    @PostMapping("/tasks")
    public String createTask(@Valid @ModelAttribute Task task, BindingResult bindingResult, @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        User currentUser = getCurrentUser(userDetails);
        task.setUser(currentUser);
        taskRepository.save(task);
        return "redirect:/";
    }

    @GetMapping("/tasks/{id}")
    public String showTaskDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        model.addAttribute("username", currentUser.getUsername());
        return taskRepository.findByIdAndUser(id, currentUser)
                .map(task -> {
                    model.addAttribute("task", task);
                    return "details";
                })
                .orElse("redirect:/"); // Or show an error page
    }

    @GetMapping("/tasks/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        model.addAttribute("username", currentUser.getUsername());
        return taskRepository.findByIdAndUser(id, currentUser)
                .map(task -> {
                    model.addAttribute("task", task);
                    return "edit";
                })
                .orElse("redirect:/"); // Or show an error page
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id, @Valid @ModelAttribute Task task, BindingResult bindingResult, @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return "edit";
        }
        User currentUser = getCurrentUser(userDetails);
        return taskRepository.findByIdAndUser(id, currentUser)
                .map(existingTask -> {
                    existingTask.setTitle(task.getTitle());
                    existingTask.setDescription(task.getDescription());
                    existingTask.setCompleted(task.isCompleted());
                    taskRepository.save(existingTask);
                    return "redirect:/tasks/" + id;
                })
                .orElse("redirect:/"); // Or show an error page
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        taskRepository.findByIdAndUser(id, currentUser).ifPresent(taskRepository::delete);
        return "redirect:/";
    }

}
