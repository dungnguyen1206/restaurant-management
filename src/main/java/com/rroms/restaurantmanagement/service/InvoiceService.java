package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.response.revenue.InvoiceResponseForManager;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryCard;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueTrendResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceService {
    RevenueSummaryCard getRevenueSummaryCard(LocalDate startDate, LocalDate endDate, String filerType, String filerCode);
    RevenueSummaryCard getRevenueSummaryCardByProcedure(LocalDate startDate, LocalDate endDate, String filerType, String filerCode);
    Page<InvoiceResponseForManager> getInvoicesByPeriod(LocalDate startDate, LocalDate endDate, String filerType, String filerCode, Integer pageNumber, Integer pageSize);
    List<RevenueTrendResponse> getRevenueTrend(String trendRange);
//    List<RevenueTrendResponse> getRevenueTrendByProcedure(String trendRange);
}
