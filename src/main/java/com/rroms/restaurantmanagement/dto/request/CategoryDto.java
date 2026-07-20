package com.rroms.restaurantmanagement.dto.request;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long categoryId;

    private String categoryName;

}