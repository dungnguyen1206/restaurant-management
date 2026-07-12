package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import com.rroms.restaurantmanagement.repository.projection.TableView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {

    @Query(value = """
            SELECT
                t.table_id AS tableId,
                t.table_number AS tableNumber,
                t.status AS status,
                t.area AS area,
                t.capacity AS capacity
            FROM restaurant_tables t
            WHERE (:keyword IS NULL OR CAST(t.table_number AS nvarchar) LIKE '%' + :keyword + '%')
            AND (:area IS NULL OR t.area = :area)
            AND (:capacity IS NULL OR t.capacity = :capacity)
            ORDER BY t.table_number ASC
            """, nativeQuery = true)
    List<TableView> filterTable(
            @Param("keyword") String keyword,
            @Param("area") String area,
            @Param("capacity") Integer capacity
    );

    List<RestaurantTable> findByStatusAndCapacityGreaterThanEqualOrderByTableNumberAsc(
            TableStatus status,
            Integer capacity
    );
}