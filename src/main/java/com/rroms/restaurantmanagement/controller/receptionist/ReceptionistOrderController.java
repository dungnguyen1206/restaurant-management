package com.rroms.restaurantmanagement.controller.receptionist;

import com.rroms.restaurantmanagement.repository.projection.OrderListProjection;
import com.rroms.restaurantmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/receptionist/orders")
@RequiredArgsConstructor
public class ReceptionistOrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<OrderListProjection> orderPage = orderService.getReceptionistOrderList(keyword, status, page, size);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("currentPageName", "orders");
        model.addAttribute("pageTitle", "Order Management");
        model.addAttribute("currentPage", "orders");
        return "receptionist/orders";
    }

    @PostMapping("/{id}/checkout")
    public String checkoutOrder(@PathVariable Long id) {
        return "redirect:/receptionist/orders";
    }
}