package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {

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

    @Query(value = "SELECT DISTINCT o FROM Order o " +
                   "LEFT JOIN FETCH o.orderItems oi " +
                   "LEFT JOIN FETCH oi.menuItem mi " +
                   "LEFT JOIN FETCH o.table t " +
                   "WHERE o.status IN :statuses " +
                   "AND o.createdAt BETWEEN :start AND :end " +
                   "AND o.orderId = :orderId",
           countQuery = "SELECT COUNT(DISTINCT o) FROM Order o " +
                        "WHERE o.status IN :statuses " +
                        "AND o.createdAt BETWEEN :start AND :end " +
                        "AND o.orderId = :orderId")
    Page<Order> getKitchenOrdersWithId(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("orderId") Long orderId,
            Pageable pageable
    );

    @Query(value = "SELECT DISTINCT o FROM Order o " +
                   "LEFT JOIN FETCH o.orderItems oi " +
                   "LEFT JOIN FETCH oi.menuItem mi " +
                   "LEFT JOIN FETCH o.table t " +
                   "WHERE o.status IN :statuses " +
                   "AND o.createdAt BETWEEN :start AND :end",
           countQuery = "SELECT COUNT(DISTINCT o) FROM Order o " +
                        "WHERE o.status IN :statuses " +
                        "AND o.createdAt BETWEEN :start AND :end")
    Page<Order> getKitchenOrdersWithoutId(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem mi " +
            "WHERE o.orderId = :id"
            )
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderId = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    long countByStatusAndCreatedAtBetween(OrderStatus status, Instant start, Instant end);

    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, Instant start, Instant end);

    @Query("SELECT oi.menuItem.itemName, SUM(oi.quantity), oi.menuItem.imageUrl " +
           "FROM Order o JOIN o.orderItems oi " +
           "WHERE o.status <> :excludedStatus AND o.createdAt BETWEEN :start AND :end " +
           "GROUP BY oi.menuItem.itemName, oi.menuItem.imageUrl " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getTopMenuItemsToday(
            @Param("excludedStatus") OrderStatus excludedStatus,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    List<Order> findLatestOrdersToday(
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );
}
