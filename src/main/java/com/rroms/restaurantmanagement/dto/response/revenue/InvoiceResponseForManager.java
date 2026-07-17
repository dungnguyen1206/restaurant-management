package com.rroms.restaurantmanagement.dto.response.revenue;

import com.rroms.restaurantmanagement.entity.constant.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Builder
public class InvoiceResponseForManager {
    private Long id;
    private BigDecimal deposit;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private List<String> paymentMethods;
    private Instant paymentDate;
    private Long orderId;
}
