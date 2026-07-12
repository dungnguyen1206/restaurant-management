package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.repository.projection.OrderListProjection;
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


    @Query(
            value = """
            SELECT
                o.order_id AS orderId,

                COALESCE(o.totalAmount, o.total_amount, 0) AS totalAmount,

                o.status AS status,

                CAST(
                    SWITCHOFFSET(o.created_at, '+00:00')
                    AS datetime2
                ) AS createdAtUtc,

                CAST(
                    SWITCHOFFSET(o.updated_at, '+00:00')
                    AS datetime2
                ) AS updatedAtUtc,

                o.created_by AS createdBy,
                o.updated_by AS updatedBy,

                t.table_id AS tableId,
                t.table_number AS tableNumber,

                u.user_id AS userId,
                u.username AS username,

                ISNULL(SUM(oi.quantity), 0) AS totalItems

            FROM orders o

            LEFT JOIN restaurant_tables t
                ON o.table_table_id = t.table_id

            LEFT JOIN users u
                ON o.user_user_id = u.user_id

            LEFT JOIN order_items oi
                ON o.order_id = oi.order_order_id

            WHERE (
                :keyword IS NULL
                OR :keyword = ''
                OR CAST(o.order_id AS VARCHAR(50))
                    LIKE CONCAT('%', :keyword, '%')
                OR t.table_number
                    LIKE CONCAT('%', :keyword, '%')
                OR u.username
                    LIKE CONCAT('%', :keyword, '%')
            )

            AND (
                :status IS NULL
                OR :status = ''
                OR o.status = :status
            )

            GROUP BY
                o.order_id,
                o.totalAmount,
                o.total_amount,
                o.status,
                o.created_at,
                o.updated_at,
                o.created_by,
                o.updated_by,
                t.table_id,
                t.table_number,
                u.user_id,
                u.username

            ORDER BY o.created_at DESC
            """,

            countQuery = """
            SELECT COUNT(*)

            FROM orders o

            LEFT JOIN restaurant_tables t
                ON o.table_table_id = t.table_id

            LEFT JOIN users u
                ON o.user_user_id = u.user_id

            WHERE (
                :keyword IS NULL
                OR :keyword = ''
                OR CAST(o.order_id AS VARCHAR(50))
                    LIKE CONCAT('%', :keyword, '%')
                OR t.table_number
                    LIKE CONCAT('%', :keyword, '%')
                OR u.username
                    LIKE CONCAT('%', :keyword, '%')
            )

            AND (
                :status IS NULL
                OR :status = ''
                OR o.status = :status
            )
            """,

            nativeQuery = true
    )
    Page<OrderListProjection> getReceptionistOrderList(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

}
