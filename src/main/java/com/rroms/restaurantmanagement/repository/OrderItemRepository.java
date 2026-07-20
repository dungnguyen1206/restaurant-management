package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
