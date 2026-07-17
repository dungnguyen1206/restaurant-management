package com.rroms.restaurantmanagement.dto.response;

import java.math.BigDecimal;


import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemDTO {
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
