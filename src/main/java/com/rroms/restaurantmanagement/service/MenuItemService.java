package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rroms.restaurantmanagement.criteria.MenuItemCriteria;
import com.rroms.restaurantmanagement.dto.request.MenuItemDto;
import com.rroms.restaurantmanagement.entity.MenuItem;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MenuItemService {
    Page<MenuItem> getAllMenuItems(String name, Long categoryId, Pageable pageable);
    void updateVirtualStock(Long itemId, Integer virtualInStock);

    Page<MenuItemDto> searchMenu(
            MenuItemCriteria criteriaMenuItem,

            int page,

            int size
    );
}
