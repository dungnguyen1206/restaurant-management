package com.rroms.restaurantmanagement.dto.response.revenue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTrendResponse {
    private String label;
    private BigDecimal revenue;
}
