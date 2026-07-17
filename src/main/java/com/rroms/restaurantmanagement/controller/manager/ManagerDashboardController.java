package com.rroms.restaurantmanagement.controller.manager;

import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryCard;
import com.rroms.restaurantmanagement.service.InvoiceService;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final InvoiceService invoiceService;
    private final ReservationService reservationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        RevenueSummaryCard revenueSummaryCard = invoiceService.getRevenueSummaryCard(LocalDate.now(),LocalDate.now(),"PRESENT","DAY");
        Long totalReservation = reservationService.countTodayReservation(LocalDate.now().atStartOfDay(), LocalDate.now().atStartOfDay().plusDays(1));

        model.addAttribute("totalReservation", totalReservation);
        model.addAttribute("totalOrder", revenueSummaryCard.getTotalOrders());
        model.addAttribute("todayRevenue", revenueSummaryCard.getTotalAmount());
        return "manager/dashboard";
    }

}
