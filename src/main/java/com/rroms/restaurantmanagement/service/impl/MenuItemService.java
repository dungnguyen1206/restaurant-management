package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService implements MenuItemRepository {

    @Override
    public List<MenuItem> findAll() {
        return List.of();
    }


}
