package com.example.my_todo_app.controller;

import com.example.my_todo_app.model.Task;
import com.example.my_todo_app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/")
    public String listTasks(Model model) {
        model.addAttribute("tasks", taskRepository.findAll());
        return "index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new Task());
        return "create";
    }

    @GetMapping("/tasks/{id}")
    public String showTaskDetails(@PathVariable Long id, Model model) {
        taskRepository.findById(id).ifPresent(task -> model.addAttribute("task", task));
        return "details";
    }

    @GetMapping("/tasks/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        taskRepository.findById(id).ifPresent(task -> model.addAttribute("task", task));
        return "edit";
    }

    @PostMapping("/tasks")
    public String createTask(@Valid @ModelAttribute Task task, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "create"; // エラーがあればフォームに戻る
        }
        taskRepository.save(task);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id, @Valid @ModelAttribute Task task, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // エラーがあれば編集フォームに戻る
            return "edit";
        }
        task.setId(id); // URLから取得したIDをタスクオブジェクトに設定
        taskRepository.save(task);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return "redirect:/";
    }

}
