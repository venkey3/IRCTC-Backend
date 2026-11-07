package com.venkatesh.irctc.controller;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class PageModelAdvice {

    @Value("${booking.window.days:60}")
    private int bookingWindowDays;

    @ModelAttribute
    public void addNavigationState(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("minTravelDate", today.toString());
        model.addAttribute("maxTravelDate", today.plusDays(bookingWindowDays).toString());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("currentUsername", null);
            model.addAttribute("currentUserIsAdmin", false);
            return;
        }

        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("currentUserIsAdmin", authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }
}
