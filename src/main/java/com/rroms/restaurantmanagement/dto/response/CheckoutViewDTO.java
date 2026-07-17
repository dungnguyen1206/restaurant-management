package com.rroms.restaurantmanagement.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutViewDTO {
    private Long orderId;
    private String tableNumber;
    private String username;
    private BigDecimal totalAmount;
    private String transferContent;
    private String vietQrUrl;
    private List<CheckoutItemDTO> items;
}
