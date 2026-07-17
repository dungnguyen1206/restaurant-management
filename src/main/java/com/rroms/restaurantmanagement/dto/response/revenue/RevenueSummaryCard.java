package com.rroms.restaurantmanagement.dto.response.revenue;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueSummaryCard {

    private BigDecimal totalAmount;
    private Long totalOrders;
    private BigDecimal averagePayment;

    public RevenueSummaryCard(BigDecimal totalAmount, Long totalOrders) {
        this.totalAmount = totalAmount;
        this.totalOrders = totalOrders;
    }
}
