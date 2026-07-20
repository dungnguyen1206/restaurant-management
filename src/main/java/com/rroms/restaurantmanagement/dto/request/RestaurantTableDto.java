package com.rroms.restaurantmanagement.dto.request;

import lombok.*;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTableDto {

    private Long tableId;

    private Integer tableNumber;

    private Integer capacity;

    private String area;

    private String status;



}