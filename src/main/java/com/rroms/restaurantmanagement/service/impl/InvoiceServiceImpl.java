package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.revenue.InvoiceResponseForManager;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryCard;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueSummaryProjection;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueTrendProjection;
import com.rroms.restaurantmanagement.dto.response.revenue.RevenueTrendResponse;
import com.rroms.restaurantmanagement.entity.Invoice;
import com.rroms.restaurantmanagement.exception.DataConflictException;
import com.rroms.restaurantmanagement.repository.InvoiceRepository;
import com.rroms.restaurantmanagement.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public RevenueSummaryCard getRevenueSummaryCard(LocalDate startDate, LocalDate endDate, String filerType, String filerCode) {
        LocalDate today = LocalDate.now();

        if ("DATA_RANGE".equalsIgnoreCase(filerType)) {
            if (startDate == null || endDate == null) {
                return getSummaryTotalByPeriod(today, today);
            }
            if (startDate.isAfter(endDate)) {
                throw new DataConflictException("Ngày bắt đầu phải trước ngày kết thúc");
            }
            return getSummaryTotalByPeriod(startDate, endDate);
        } else if ("PRESET".equalsIgnoreCase(filerType)) {
            if ("DAY".equalsIgnoreCase(filerCode)) {
                return getSummaryTotalByPeriod(today, today);
            } else if ("MONTH".equalsIgnoreCase(filerCode)) {
                return getSummaryTotalByPeriod(today.with(TemporalAdjusters.firstDayOfMonth()), today.with(TemporalAdjusters.lastDayOfMonth()));
            } else if ("WEEK".equalsIgnoreCase(filerCode)) {
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                return getSummaryTotalByPeriod(startOfWeek, endOfWeek);
            }
        }
        return getSummaryTotalByPeriod(today, today);
    }

//    @Override
//    public List<RevenueTrendResponse> getRevenueTrendByProcedure(String trendRange) {
//        List<RevenueTrendProjection> trendProjections =
//                invoiceRepository.getRevenueTrendByProcedure(trendRange);
//
//        return trendProjections.stream()
//                .map(trend -> RevenueTrendResponse.builder()
//                        .label(toTrendLabel(trend, trendRange))
//                        .revenue(trend.getRevenue() == null ? BigDecimal.ZERO : trend.getRevenue())
//                        .build())
//                .toList();
//    }

    @Override
    public RevenueSummaryCard getRevenueSummaryCardByProcedure(LocalDate startDate, LocalDate endDate, String filerType, String filerCode) {
        LocalDate today = LocalDate.now();

        if ("DATA_RANGE".equalsIgnoreCase(filerType)) {
            if (startDate == null || endDate == null) {
                return getSummaryTotalByPeriodUsingProcedure(today, today);
            }
            if (startDate.isAfter(endDate)) {
                throw new DataConflictException("Ngày bắt đầu phải trước ngày kết thúc");
            }
            return getSummaryTotalByPeriodUsingProcedure(startDate, endDate);
        } else if ("PRESET".equalsIgnoreCase(filerType)) {
            if ("DAY".equalsIgnoreCase(filerCode)) {
                return getSummaryTotalByPeriodUsingProcedure(today, today);
            } else if ("MONTH".equalsIgnoreCase(filerCode)) {
                return getSummaryTotalByPeriodUsingProcedure(today.with(TemporalAdjusters.firstDayOfMonth()), today.with(TemporalAdjusters.lastDayOfMonth()));
            } else if ("WEEK".equalsIgnoreCase(filerCode)) {
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                return getSummaryTotalByPeriodUsingProcedure(startOfWeek, endOfWeek);
            }
        }
        return getSummaryTotalByPeriodUsingProcedure(today, today);
    }

    private RevenueSummaryCard getSummaryTotalByPeriod(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        RevenueSummaryCard revenueSummaryCard = invoiceRepository.getSummaryTotalByPeriod(start, end);
        if (revenueSummaryCard.getTotalAmount() == null) {
            revenueSummaryCard.setTotalAmount(BigDecimal.ZERO);
            revenueSummaryCard.setAveragePayment(BigDecimal.ZERO);
        } else {
            BigDecimal average = (revenueSummaryCard.getTotalOrders() == null || revenueSummaryCard.getTotalOrders() == 0)
                    ? BigDecimal.ZERO
                    : revenueSummaryCard.getTotalAmount().divide(BigDecimal.valueOf(revenueSummaryCard.getTotalOrders()), 2, RoundingMode.HALF_UP);
            revenueSummaryCard.setAveragePayment(average);
        }
        return revenueSummaryCard;
    }

    private RevenueSummaryCard getSummaryTotalByPeriodUsingProcedure(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        RevenueSummaryProjection projection =null;
        BigDecimal totalAmount = projection.getTotalAmount() == null ? BigDecimal.ZERO : projection.getTotalAmount();
        Long totalOrders = projection.getTotalOrder() == null ? 0L : projection.getTotalOrder();
        BigDecimal averagePayment = projection.getAvgRevenue() == null ? BigDecimal.ZERO : projection.getAvgRevenue();

        return RevenueSummaryCard.builder()
                .totalAmount(totalAmount)
                .totalOrders(totalOrders)
                .averagePayment(averagePayment)
                .build();
    }

    @Override
    public Page<InvoiceResponseForManager> getInvoicesByPeriod(LocalDate startDate, LocalDate endDate, String filerType, String filerCode, Integer page, Integer size) {
        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(page, size);

        if ("DATA_RANGE".equalsIgnoreCase(filerType)) {
            if (startDate == null || endDate == null) {
                return invoiceRepository.getInvoicesByPeriod(toInstantType(today), toInstantType(today.plusDays(1)), pageable)
                        .map(this::toInvoiceResponseForManager);
            }
            if (startDate.isAfter(endDate)) {
                throw new DataConflictException("Ngày bắt đầu phải trước ngày kết thúc");
            }
            return invoiceRepository.getInvoicesByPeriod(toInstantType(startDate), toInstantType(endDate), pageable)
                    .map(this::toInvoiceResponseForManager);
        } else if ("PRESET".equalsIgnoreCase(filerType)) {
            if ("DAY".equalsIgnoreCase(filerCode)) {
                return invoiceRepository.getInvoicesByPeriod(toInstantType(today), toInstantType(today.plusDays(1)), pageable)
                        .map(this::toInvoiceResponseForManager);
            } else if ("MONTH".equalsIgnoreCase(filerCode)) {
                return invoiceRepository.getInvoicesByPeriod(toInstantType(today.with(TemporalAdjusters.firstDayOfMonth())), toInstantType(today.with(TemporalAdjusters.lastDayOfMonth())), pageable)
                        .map(this::toInvoiceResponseForManager);
            } else if ("WEEK".equalsIgnoreCase(filerCode)) {
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                return invoiceRepository.getInvoicesByPeriod(toInstantType(startOfWeek), toInstantType(endOfWeek), pageable)
                        .map(this::toInvoiceResponseForManager);
            }
        }
        return invoiceRepository.getInvoicesByPeriod(toInstantType(today), toInstantType(today.plusDays(1)), pageable)
                .map(this::toInvoiceResponseForManager);
    }

    @Override
    public List<RevenueTrendResponse> getRevenueTrend(String trendRange) {
        LocalDate today = LocalDate.now();
        Instant start;
        Instant end;
        //List interface ở compile-time, nhưng runtime là list proxy objects implement interface đó.
        //Phần này liên quan đến 1 proxy object
        List<RevenueTrendProjection> trendProjections;

        if ("YEAR".equalsIgnoreCase(trendRange)) {
            start = toInstantType(LocalDate.of(2020, 1, 1));
            end = toInstantType(today.with(TemporalAdjusters.firstDayOfYear()).plusYears(1));
            trendProjections = invoiceRepository.getRevenueTrendByYear(start, end);
        } else if ("MONTH".equalsIgnoreCase(trendRange)) {
            start = toInstantType(today.with(TemporalAdjusters.firstDayOfYear()));
            end = toInstantType(today.with(TemporalAdjusters.firstDayOfYear()).plusYears(1));
            trendProjections = invoiceRepository.getRevenueTrendByMonth(start, end);
        } else {
            start = toInstantType(today.minusDays(6));
            end = toInstantType(today.plusDays(1));
            trendProjections = invoiceRepository.getRevenueTrendByDay(start, end);
        }

        return trendProjections.stream()
                .map(trend -> RevenueTrendResponse.builder()
                        .label(toTrendLabel(trend, trendRange))
                        .revenue(trend.getRevenue() == null ? BigDecimal.ZERO : trend.getRevenue())
                        .build())
                .toList();
    }

    private String toTrendLabel(RevenueTrendProjection trend, String trendRange) {
        if ("YEAR".equalsIgnoreCase(trendRange)) {
            return String.valueOf(trend.getYearNumber());
        }
        if ("MONTH".equalsIgnoreCase(trendRange)) {
            return String.format("%02d/%d", trend.getMonthNumber(), trend.getYearNumber());
        }
        return String.format("%02d/%02d", trend.getDayNumber(), trend.getMonthNumber());
    }

    private Instant toInstantType(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public InvoiceResponseForManager toInvoiceResponseForManager(Invoice invoice) {
        return InvoiceResponseForManager.builder()
                .id(invoice.getInvoiceId())
                .deposit(invoice.getDeposit())
                .totalAmount(invoice.getTotal())
                .finalAmount(invoice.getFinalAmount())
                .paymentMethods(invoice.getPayments().stream().map(payment -> payment.getPaymentMethod().name()).distinct().toList())
                .paymentDate(invoice.getPaidAt())
                .orderId(invoice.getOrder() == null ? null : invoice.getOrder().getOrderId())
                .build();
    }
}
