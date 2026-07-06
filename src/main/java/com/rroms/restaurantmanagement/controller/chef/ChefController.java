package com.rroms.restaurantmanagement.controller.chef;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/chef")
@RequiredArgsConstructor
public class ChefController {

    private final OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboardChef(Model model){
        model.addAttribute("activePage", "dashboard");
        return "chef/dashboard";
    }

    @GetMapping("/orders")
    public String processOrderPage(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Order> orderPage = orderService.getKitchenOrders(orderId, status, pageable);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentStatus", status != null ? status : "");
        model.addAttribute("currentOrderId", orderId != null ? orderId : "");
        model.addAttribute("activePage", "orders");
        return "chef/orders";
    }

    @PostMapping("/orders/{id}/confirm")
    public String chefOrderConfirm(
            @PathVariable Long id
    ) {
        this.orderService.handleUpdateStatusOrder(id, OrderStatus.PREPARING);
        return "redirect:/chef/orders";
    }

    @PostMapping("/orders/{id}/done")
    public String chefOrderDone(
            @PathVariable Long id
    ) {
        this.orderService.handleUpdateStatusOrder(id, OrderStatus.COMPLETED);
        return "redirect:/chef/orders";
    }

    @PostMapping("/orders/{id}/cancel")
    public String chefOrderCancel(
            @PathVariable Long id
    ) {
        this.orderService.handleUpdateStatusOrder(id, OrderStatus.CANCELLED);
        return "redirect:/chef/orders";
    }


    @GetMapping("/history")
    public String chefOrderHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "status", required = false) String status,
            Model model){
        model.addAttribute("activePage", "order-history");
        Page<OrderHistoryDTO> orderPage = orderService.searchChefOrderHistory(
                keyword, startDate, endDate, status, page, size
        );
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "chef/order-history";
    }
}
