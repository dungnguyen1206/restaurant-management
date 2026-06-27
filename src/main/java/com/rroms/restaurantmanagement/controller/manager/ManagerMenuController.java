package com.rroms.restaurantmanagement.controller.manager;

import com.rroms.restaurantmanagement.dto.response.CategoryResponseForManager;
import com.rroms.restaurantmanagement.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/manager/menu")
@RequiredArgsConstructor
public class ManagerMenuController {
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model) {

        List<CategoryResponseForManager> categoryResponseForManagers = categoryService.findAll();
        model.addAttribute("categoryResponseForManagers", categoryResponseForManagers);

        return "manager/menu/list";
    }
}
