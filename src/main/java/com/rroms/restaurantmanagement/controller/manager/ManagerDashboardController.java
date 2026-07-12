package com.rroms.restaurantmanagement.controller.manager;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerDashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "manager/dashboard";
    }
}
