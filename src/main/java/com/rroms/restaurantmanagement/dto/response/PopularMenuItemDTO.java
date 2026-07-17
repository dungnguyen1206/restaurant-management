package com.rroms.restaurantmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PopularMenuItemDTO {
    private String itemName;
    private long totalOrdered;
    private String imageUrl;
}
