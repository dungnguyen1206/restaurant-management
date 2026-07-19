package com.rroms.restaurantmanagement.controller.customer;

import com.rroms.restaurantmanagement.criteria.MenuItemCriteria;
import com.rroms.restaurantmanagement.dto.request.MenuItemDto;
import com.rroms.restaurantmanagement.service.impl.CategoryServiceImpl;
import com.rroms.restaurantmanagement.service.impl.MenuItemServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class HomeController {

    private final MenuItemServiceImpl menuItemService;
    private final CategoryServiceImpl categoryService;

    public HomeController(MenuItemServiceImpl menuItemService, CategoryServiceImpl categoryService) {
        this.menuItemService = menuItemService;
        this.categoryService = categoryService;
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("menuItems", menuItemService.findMostPopular(6));
        return "home";
    }

    @GetMapping("/menu")
    public String menuPage(
            @ModelAttribute("criteriaMenuItem") MenuItemCriteria menuItemCriteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model
    ) {

        Page<MenuItemDto> result = menuItemService.searchMenu(
                menuItemCriteria,
                page,
                size
        );

        int start;
        int end;
        int currentPage = result.getNumber();
        int totalPages = result.getTotalPages();

        if (currentPage - 2 < 0) {

            start = 0;
            end = Math.min(4, totalPages - 1);

        } else if (currentPage + 2 >= totalPages) {

            start = Math.max(0, totalPages - 5);
            end = totalPages - 1;

        } else {

            start = currentPage - 2;
            end = currentPage + 2;

        }


        model.addAttribute("menuItems", result.getContent());
        model.addAttribute("categories", categoryService.findAllForCustomer());

        model.addAttribute("currentPage", currentPage);

        model.addAttribute("totalPages", totalPages);
        model.addAttribute("criteriaMenuItem", menuItemCriteria);
        model.addAttribute("startPage", start);
        model.addAttribute("endPage", end);


        return "menu";

    }

    @GetMapping("/search")
    public String reservationPage(Model model) {
        return "redirect:/customer/reservations/available";
    }

    @GetMapping("/reservations/search")
    public String search(
            @RequestParam(required = false)
            LocalDate date,
            Model model
    ) {
        String target = "/customer/reservations/available";
        return date == null ? "redirect:" + target : "redirect:" + target + "?date=" + date;
    }


}
