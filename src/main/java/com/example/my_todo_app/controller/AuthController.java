package com.example.my_todo_app.controller;

import com.example.my_todo_app.model.User;
import com.example.my_todo_app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session, @RequestParam(value = "error", required = false) String error) {
        User loginForm = new User();

        AuthenticationException ex = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (ex != null && error != null) {
            model.addAttribute("errorMessage", "ユーザー名かパスワードが間違っています");
            // 認証失敗時に保持されていた認証情報からユーザー名を取得
            if (ex.getAuthenticationRequest() != null) {
                loginForm.setUsername(ex.getAuthenticationRequest().getName());
            }
        }
        model.addAttribute("loginForm", loginForm);
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
                               RedirectAttributes redirectAttributes) {
        // ユーザー名の重複チェック
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            result.addError(new org.springframework.validation.FieldError("user", "username", "このユーザー名は既に使用されています"));
        }

        if (result.hasErrors()) {
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage",
                "ユーザー登録が完了しました");

        return "redirect:/login";
    }
}