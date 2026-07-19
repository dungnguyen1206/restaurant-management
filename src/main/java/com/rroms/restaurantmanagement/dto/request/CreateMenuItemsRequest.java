package com.rroms.restaurantmanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMenuItemsRequest {
    private Long itemId;
    @NotNull(message = "Không bỏ trống tên món ăn")
    private String itemName;
    private String itemDescription;

    @NotNull(message = "Giá tiền không được bỏ trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá món ăn không được nhỏ hơn 0")
    private BigDecimal itemPrice;
    private String itemImageUrl;
    private Long categoryId;
}
