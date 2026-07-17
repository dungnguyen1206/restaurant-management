package com.rroms.restaurantmanagement.controller.receptionist;

import com.rroms.restaurantmanagement.repository.projection.OrderListProjection;
import com.rroms.restaurantmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.rroms.restaurantmanagement.entity.constant.PaymentMethod;
import com.rroms.restaurantmanagement.service.CheckoutService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/receptionist/orders")
@RequiredArgsConstructor
public class ReceptionistOrderController {

    private final OrderService orderService;
    private final CheckoutService checkoutService;
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

    @GetMapping("/{id}/checkout")
    public String checkoutPage(@PathVariable Long id, Model model) {
        model.addAttribute("checkout", checkoutService.getCheckoutView(id));
        model.addAttribute("pageTitle", "Checkout");
        model.addAttribute("currentPage", "orders");

        return "receptionist/checkout";
    }

    @PostMapping("/{id}/checkout/vietqr")
    public String confirmVietQrPaid(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            checkoutService.confirmPaid(id, PaymentMethod.BANK_TRANSFER);
            redirectAttributes.addFlashAttribute("success", "Checkout VietQR thành công");
            return "redirect:/receptionist/tables";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/receptionist/orders/" + id + "/checkout";
        }
    }

    @PostMapping("/{id}/checkout/cash")
    public String confirmCashPaid(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            checkoutService.confirmPaid(id, PaymentMethod.CASH);
            redirectAttributes.addFlashAttribute("success", "Checkout tiền mặt thành công");
            return "redirect:/receptionist/tables";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/receptionist/orders/" + id + "/checkout";
        }
    }
}