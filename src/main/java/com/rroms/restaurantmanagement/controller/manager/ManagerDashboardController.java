package com.rroms.restaurantmanagement.controller.manager;

import com.rroms.restaurantmanagement.dto.response.ReservationResponseForManager;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryCard;
import com.rroms.restaurantmanagement.service.InvoiceService;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final InvoiceService invoiceService;
    private final ReservationService reservationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        RevenueSummaryCard revenueSummaryCard = invoiceService.getRevenueSummaryCard(LocalDate.now(),LocalDate.now(),"PRESENT","DAY");
        Long totalReservation = reservationService.countTodayReservation(LocalDate.now().atStartOfDay(), LocalDate.now().atStartOfDay().plusDays(1));
        Page<ReservationResponseForManager> reservations = reservationService.getAllTodayReservationsForManager(LocalDate.now().atStartOfDay(),  LocalDate.now().atStartOfDay().plusDays(1), page, size);

        model.addAttribute("reservations", reservations.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reservations.getTotalPages());
        model.addAttribute("totalReservation", totalReservation);
        model.addAttribute("totalOrder", revenueSummaryCard.getTotalOrders());
        model.addAttribute("todayRevenue", revenueSummaryCard.getTotalAmount());
        return "manager/dashboard";
    }

}
