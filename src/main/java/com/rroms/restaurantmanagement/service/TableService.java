package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.repository.projection.TableView;
import org.springframework.stereotype.Service;

import java.util.List;
public interface TableService {
    List<TableView> filterTables(String keyword, String area, String capacity);

    RestaurantTable getTableById(Long tableId);

    List<User> getWaiters();

    void assignWaiter(Long tableId, Long waiterId);

    List<RestaurantTable> getTablesByWaiter(Long waiterId);





}
