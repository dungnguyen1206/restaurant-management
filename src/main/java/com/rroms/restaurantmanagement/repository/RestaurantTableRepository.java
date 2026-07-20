package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    @Query("""
            SELECT t FROM RestaurantTable t
            WHERE t.status = :tableStatus
              AND (:capacity IS NULL OR t.capacity >= :capacity)
              AND NOT EXISTS (
                SELECT rt.reservationTableId FROM ReservationTable rt
                WHERE rt.table = t
                  AND rt.reservation.reservationTime >= :startTime
                  AND rt.reservation.reservationTime < :endTime
                  AND rt.reservation.status IN :statuses
              )
            ORDER BY t.tableNumber
            """)
    List<RestaurantTable> findAvailableTables(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("tableStatus") TableStatus tableStatus,
            @Param("capacity") Integer capacity,
            @Param("statuses") List<ReservationStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RestaurantTable t " +
            "WHERE t.tableId IN :ids " +
            "ORDER BY t.tableId")
    List<RestaurantTable> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
