package com.rroms.restaurantmanagement.controller.manager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager/menu")
public class ManagerMenuController {

    @GetMapping("/list")
    public String list(Model model) {
        return "manager/menu/list";
    }
}
