package com.rroms.restaurantmanagement.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDto {

    private Long itemId;

    private String itemName;

    private BigDecimal price;

    private String description;

    private String imageUrl;

    private Boolean isSoldOut;

    private Integer virtualInStock;

    private Long categoryId;

    private String categoryName;

}