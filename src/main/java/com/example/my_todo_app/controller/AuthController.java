package com.example.my_todo_app.controller;

import com.example.my_todo_app.model.User;
import com.example.my_todo_app.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model, String error) {
        if (error != null) {
            model.addAttribute("errorMessage", "ユーザー名かパスワードが間違っています");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
        // ユーザー名の重複チェック
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            result.addError(new org.springframework.validation.FieldError("user", "username", "このユーザー名は既に使用されています"));
        }

        if (result.hasErrors()) {
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }
}