package com.rroms.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemResponseForManager {
    private Long id;
    private String itemName;
    private String itemDescription;
    private BigDecimal itemPrice;
    private String image;
}
