package com.rroms.restaurantmanagement.dto.response;

import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class OrderHistoryDTO {
    private Long orderId;
    private Integer tableNumber;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private Instant createdAt;
    private Long totalItems;
}
