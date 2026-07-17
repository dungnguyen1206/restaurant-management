package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuItemService {
    Page<MenuItem> getAllMenuItems(String name, Long categoryId, Pageable pageable);
    void updateVirtualStock(Long itemId, Integer virtualInStock);
}
