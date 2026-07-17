package com.rroms.restaurantmanagement.controller.manager;

import com.rroms.restaurantmanagement.dto.response.revenue.InvoiceResponseForManager;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryCard;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueTrendResponse;
import com.rroms.restaurantmanagement.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/revenue")
public class ManagerInvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping
    public String revenue(@RequestParam(defaultValue = "PRESET") String filterType,
                          @RequestParam(defaultValue = "DAY") String filterCode,
                          @RequestParam(required = false) LocalDate startDate,
                          @RequestParam(required = false) LocalDate endDate,
                          @RequestParam(defaultValue = "LAST_7_DAYS") String trendRange,
                          @RequestParam(defaultValue = "0") Integer page,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          Model model) {

        RevenueSummaryCard summaryCard = invoiceService.getRevenueSummaryCard(startDate, endDate, filterType, filterCode);
        Page<InvoiceResponseForManager> invoiceResponseForManagers = invoiceService.getInvoicesByPeriod(startDate,endDate,filterType,filterCode,page,pageSize);
        List<RevenueTrendResponse> revenueTrends = invoiceService.getRevenueTrend(trendRange);
        model.addAttribute("invoiceResponseForManagers", invoiceResponseForManagers.getContent());
        model.addAttribute("summaryCard", summaryCard);
        model.addAttribute("trendLabels", revenueTrends.stream().map(RevenueTrendResponse::getLabel).toList());
        model.addAttribute("trendRevenues", revenueTrends.stream().map(RevenueTrendResponse::getRevenue).map(BigDecimal::longValue).toList());
        model.addAttribute("filterType", filterType);
        model.addAttribute("filterCode", filterCode);
        model.addAttribute("trendRange", trendRange);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoiceResponseForManagers.getTotalPages());
        model.addAttribute("totalElement", invoiceResponseForManagers.getTotalElements());
        return "manager/revenue";
    }
}
