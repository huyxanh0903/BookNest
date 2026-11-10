package com.huypham.huymelon.spring3.controller;

import com.huypham.huymelon.spring3.entity.User;
import com.huypham.huymelon.spring3.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalSecurityAdvice {

    private final UserService userService;

    public GlobalSecurityAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        try {
            return userService.getCurrentUser(authentication.getPrincipal());
        } catch (Exception e) {
            return null;
        }
    }
}
