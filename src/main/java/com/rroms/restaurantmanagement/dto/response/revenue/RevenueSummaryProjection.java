package com.rroms.restaurantmanagement.dto.response.revenue;

import java.math.BigDecimal;

public interface RevenueSummaryProjection {

    BigDecimal getTotalAmount();
    Long getTotalOrder();
    BigDecimal getAvgRevenue();

}
