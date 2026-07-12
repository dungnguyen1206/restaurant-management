package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.MenuItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository {
    List<MenuItem> findAll();
}
