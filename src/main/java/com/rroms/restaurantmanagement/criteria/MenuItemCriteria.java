package com.rroms.restaurantmanagement.criteria;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuItemCriteria {

    private String keyword;

    private Long categoryId;

    private Boolean soldOut;

    private String sort;
}
