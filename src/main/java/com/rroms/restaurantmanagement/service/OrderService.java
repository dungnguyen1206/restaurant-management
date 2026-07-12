package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    Page<Order> getKitchenOrders(String orderId, String status, Pageable pageable);
    void handleUpdateStatusOrder(Long orderId, OrderStatus orderStatus);
    Page<OrderHistoryDTO> searchChefOrderHistory(String keyword, LocalDate startDate, LocalDate endDate, String status, int page, int size);
}
