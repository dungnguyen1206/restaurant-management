package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.MenuItem;

import java.util.List;

public interface MenuItemService {
    List<MenuItem> getAllMenuItems(String name);
}
