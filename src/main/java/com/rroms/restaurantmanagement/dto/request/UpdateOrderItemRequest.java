package com.rroms.restaurantmanagement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderItemRequest {
    private Long orderItemId;
    private Integer quantity;
    private String note;
}
