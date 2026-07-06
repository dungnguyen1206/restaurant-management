package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {
    // Dùng StatusIn thay vì Status
    Page<Order> findByStatusInAndCreatedAtBetween(
            List<OrderStatus> statuses,
            Instant start,
            Instant end,
            Pageable pageable
    );

    @Query("SELECT new com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO(" +
            "o.orderId, t.tableNumber, o.totalAmount, o.status, o.createdAt, COUNT(oi.quantity)) " +
            "FROM Order o " +
            "LEFT JOIN o.orderItems oi " +
            "LEFT JOIN o.table t " +
            "WHERE (:keyword IS NULL OR CAST(o.orderId AS string) LIKE %:keyword% OR CAST(o.status AS string) LIKE %:keyword%) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "GROUP BY o.orderId,t.tableNumber, o.totalAmount, o.status, o.createdAt"
    )
    Page<OrderHistoryDTO> getOrderHistoryByUserId(
            @Param("keyword") String keyword,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("status") OrderStatus status,
            Pageable pageable);
}
