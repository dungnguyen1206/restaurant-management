package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.RestaurantTable;

import java.time.LocalDate;
import java.util.List;

public interface RestaurantTableService {
    public List<RestaurantTable> findAvailableTables(
            LocalDate date,
            Integer slot,
            Integer capacity
    );
}
