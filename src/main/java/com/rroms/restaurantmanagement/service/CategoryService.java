package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.request.CategoryDto;
import com.rroms.restaurantmanagement.dto.response.CategoryResponseForManager;
import com.rroms.restaurantmanagement.entity.Category;

import java.util.List;

public interface CategoryService {

    public List<CategoryDto> findAllForCustomer();
    List<CategoryResponseForManager> findAll();
}
