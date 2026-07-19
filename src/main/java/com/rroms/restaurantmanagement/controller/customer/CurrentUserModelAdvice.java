package com.rroms.restaurantmanagement.controller.customer;

import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.rroms.restaurantmanagement.controller.customer")
@RequiredArgsConstructor
public class CurrentUserModelAdvice {
    private final UserRepository userRepository;

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByUsernameIgnoreCase(authentication.getName()).orElse(null);
    }
}
