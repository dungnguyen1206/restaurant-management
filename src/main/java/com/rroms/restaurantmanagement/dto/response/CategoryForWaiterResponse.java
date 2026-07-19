package com.rroms.restaurantmanagement.dto.response;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryForWaiterResponse {
    private Long categoryId;
    private String categoryName;
}
