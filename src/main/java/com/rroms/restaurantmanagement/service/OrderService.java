package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.Order;

import java.util.List;

public interface OrderService {
    public List<Order> getOrders();

    public Order getOrderById(long id);

    public Boolean addOrder(Order order);

    public Boolean updateOrder(Order order);
}
