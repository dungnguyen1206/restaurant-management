package com.rroms.restaurantmanagement.controller.waiter;

import com.rroms.restaurantmanagement.dto.request.MenuFilter;
import com.rroms.restaurantmanagement.dto.request.AddOrderItemRequest;
import com.rroms.restaurantmanagement.dto.request.UpdateOrderItemRequest;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.service.CategoryService;
import com.rroms.restaurantmanagement.service.MenuItemService;
import com.rroms.restaurantmanagement.service.OrderService;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/waiter")
@RequiredArgsConstructor
public class MenuController {
    private final MenuItemService menuItemService;
    private final CategoryService categoryService;
    private final ReservationService reservationService;
    private final OrderService orderService;

    @GetMapping("/order/create/{id}")
    // WAITER FUNCTION: hien thi menu va order hien tai cua reservation.
    public String Menus(@ModelAttribute MenuFilter menuFilter,
                        @PathVariable("id") Long id,
                        @RequestParam(required = false) String keyword,
                        @PageableDefault(size = 5, sort = "itemName") Pageable pageable,
                        @RequestParam(required = false) Long categoryId,
                        Model model){
        menuFilter.setKeyword(keyword);
        menuFilter.setCategoryId(categoryId);

        Reservation reservation = reservationService.getReservationById(id);

        Order activeOrder = orderService.getActiveOrderForReservation(id);

        model.addAttribute("reservation", reservation);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("menuItemPage", menuItemService.getMenusforWaiter(categoryId, menuFilter,pageable));
        model.addAttribute("categories", categoryService.getCategoriesforWaiter());
        model.addAttribute("activeOrder", activeOrder);
        model.addAttribute("activeOrderItems", activeOrder != null ? activeOrder.getOrderItems() : Collections.emptyList());
        return "waiter/content/Menus";
    }

    @PostMapping("/orders/from-reservation/{id}/cart/add")
    public String addToCart(@PathVariable("id") Long reservationId,
                            @ModelAttribute AddOrderItemRequest request,
                            @AuthenticationPrincipal(expression = "user") User user) {
        orderService.addItemToDraftOrder(reservationId, request, user);
        return "redirect:/waiter/order/create/" + reservationId;
    }

    @PostMapping("/orders/from-reservation/{id}/cart/update")
    public String updateCartItem(@PathVariable("id") Long reservationId,
                                 @ModelAttribute UpdateOrderItemRequest request) {
        orderService.updateDraftOrderItem(reservationId, request);
        return "redirect:/waiter/order/create/" + reservationId;
    }

    @PostMapping("/orders/from-reservation/{id}/cart/remove")
    public String removeCartItem(@PathVariable("id") Long reservationId,
                                 @RequestParam Long orderItemId) {
        orderService.removeDraftOrderItem(reservationId, orderItemId);
        return "redirect:/waiter/order/create/" + reservationId;
    }

    @PostMapping("/orders/from-reservation/{id}")
    public String sendOrderToKitchen(@PathVariable("id") Long reservationId) {
        orderService.sendOrderToKitchen(reservationId);
        return "redirect:/waiter/orders";
    }

    @PostMapping("/orders/{orderId}/items/{orderItemId}/serve")
    public String serveOrderItem(@PathVariable Long orderId,
                                 @PathVariable Long orderItemId) {
        orderService.markOrderItemServed(orderId, orderItemId);
        return "redirect:/waiter/orders/" + orderId;
    }

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal(expression = "user") User user,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
                         Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderService.getWaiterOrders(user.getUserId(), pageable);
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        return "waiter/content/Orders";
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrder(@PathVariable Long orderId,
                            @AuthenticationPrincipal(expression = "user") User user,
                            Model model) {
        Order order = orderService.findById(orderId);
        if (order == null) {
            throw new ResponseStatusException(NOT_FOUND, "Order khong ton tai");
        }
        if (order.getUser() == null || !order.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(FORBIDDEN, "Ban khong co quyen xem order nay");
        }

        model.addAttribute("order", order);
        model.addAttribute("orderItems", order.getOrderItems());
        return "waiter/content/ViewOrders";
    }

}
