package com.venkatesh.irctc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.venkatesh.irctc.dto.RegisterRequest;
import com.venkatesh.irctc.service.AuthService;

import jakarta.validation.Valid;
import java.time.LocalDate;

@Controller
public class PageAuthController {

    private final AuthService authService;

    public PageAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("today", LocalDate.now());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
            return "redirect:/login?registered";
        } catch (RuntimeException exception) {
            model.addAttribute("registrationError", "That username or email may already be registered. Please try another.");
            return "auth/register";
        }
    }
}
