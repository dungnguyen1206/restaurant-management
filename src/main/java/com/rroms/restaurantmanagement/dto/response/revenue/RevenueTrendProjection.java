package com.rroms.restaurantmanagement.dto.response.revenue;

import java.math.BigDecimal;

public interface RevenueTrendProjection {
    Integer getDayNumber();

    Integer getMonthNumber();

    Integer getYearNumber();

    BigDecimal getRevenue();
}
