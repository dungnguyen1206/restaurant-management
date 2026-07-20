package com.rroms.restaurantmanagement.controller.waiter;

import com.rroms.restaurantmanagement.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/waiter")
public class WaiterController {

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal(expression = "user") User user, Model model) {
        model.addAttribute("waiter", user);
        return "waiter/content/Profile";
    }
}
