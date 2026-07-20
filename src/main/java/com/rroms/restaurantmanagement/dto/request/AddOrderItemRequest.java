package com.rroms.restaurantmanagement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddOrderItemRequest {
    private Long itemId;
    private Integer quantity;
    private String note;
}
