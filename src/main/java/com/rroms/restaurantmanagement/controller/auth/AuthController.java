package com.rroms.restaurantmanagement.controller.auth;

import com.rroms.restaurantmanagement.dto.request.UserRegisterDTO;
import com.rroms.restaurantmanagement.exception.DuplicateEmailException;
import com.rroms.restaurantmanagement.exception.PasswordNotMatchException;
import com.rroms.restaurantmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auths")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    @GetMapping("/login")
    public String getLoginPage(
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "success", required = false) String success) {
        return "auth/login";

    }
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("userRegisterDTO") UserRegisterDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.handleCreateUser(dto);
            return "redirect:/auths/login?registerSuccess";
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue(
                    "username",
                    "duplicate",
                    e.getMessage());
        } catch (PasswordNotMatchException e) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "notMatch",
                    e.getMessage());
        }
        return "auth/register";
    }
}
