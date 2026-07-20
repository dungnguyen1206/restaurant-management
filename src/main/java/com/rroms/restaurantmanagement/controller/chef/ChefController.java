package com.rroms.restaurantmanagement.controller.chef;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.service.OrderService;
import com.rroms.restaurantmanagement.dto.response.ChefDashboardDTO;
import com.rroms.restaurantmanagement.service.MenuItemService;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.stream.Stream;

@Controller
@RequestMapping("/chef")
@RequiredArgsConstructor
public class ChefController {

    private final OrderService orderService;
    private final MenuItemService menuItemService;
    private final CategoryService categoryService;

    @GetMapping("/dashboard")
    public String dashboardChef(
            @AuthenticationPrincipal(expression = "user") User user,
            Model model
    ) {
        addChefAccountToModel(user, model);
        ChefDashboardDTO dashboardData = orderService.getChefDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("activePage", "dashboard");
        return "chef/dashboard";
    }

    @GetMapping("/orders")
    public String processOrderPage(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal(expression = "user") User user,
            Model model
    ) {
        addChefAccountToModel(user, model);
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
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        this.orderService.confirmKitchenOrder(id);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã xác nhận đơn hàng #" + id + " và cập nhật tồn kho."
        );
        return "redirect:/chef/orders";
    }

    @PostMapping("/orders/{id}/done")
    public String chefOrderDone(
            @PathVariable Long id
    ) {
        this.orderService.markKitchenOrderReady(id);
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
            @AuthenticationPrincipal(expression = "user") User user,
            Model model){
        addChefAccountToModel(user, model);
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
    @GetMapping("/history-detail/{id}")
    @Transactional(readOnly = true)
    public String getOrderDetailFragment(@PathVariable("id") Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        return "chef/order-detail :: orderDetail";
    }

    @GetMapping("/menu")
    public String chefMenu(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal(expression = "user") User user,
            Model model
    ) {
        addChefAccountToModel(user, model);
        Pageable pageable = PageRequest.of(page, size);
        Page<MenuItem> menuItemPage = menuItemService.getAllMenuItems(search, categoryId, pageable);
        model.addAttribute("menuItems", menuItemPage.getContent());
        model.addAttribute("menuItemPage", menuItemPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("activePage", "menu");
        return "chef/menu";
    }

    @PostMapping("/menu/{id}/stock")
    public String updateVirtualStock(
            @PathVariable Long id,
            @RequestParam Integer virtualInStock,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            RedirectAttributes redirectAttributes
    ) {
        menuItemService.updateVirtualStock(id, virtualInStock);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                virtualInStock == 0
                        ? "Đã cập nhật tồn kho về 0 và chuyển món sang trạng thái Hết món."
                        : "Đã cập nhật tồn kho và chuyển món sang trạng thái Còn món."
        );
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);
        if (search != null && !search.trim().isEmpty()) {
            redirectAttributes.addAttribute("search", search.trim());
        }
        if (categoryId != null) {
            redirectAttributes.addAttribute("categoryId", categoryId);
        }
        return "redirect:/chef/menu";
    }

    private void addChefAccountToModel(User user, Model model) {
        String displayName = buildFullName(user);

        model.addAttribute("chefDisplayName", displayName);
        model.addAttribute("chefEmail", user.getUsername());
        model.addAttribute("chefInitials", buildInitials(displayName));
    }

    private String buildFullName(User user) {
        String fullName = Stream.of(user.getFirstName(), user.getMiddleName(), user.getLastName())
                .filter(part -> part != null && !part.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + " " + right)
                .orElse("");
        return fullName.isBlank() ? user.getUsername() : fullName;
    }

    private String buildInitials(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "C";
        }
        String[] parts = displayName.trim().split("\\s+");
        String firstInitial = parts[0].substring(0, 1);
        String lastInitial = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (firstInitial + lastInitial).toUpperCase();
    }
}
