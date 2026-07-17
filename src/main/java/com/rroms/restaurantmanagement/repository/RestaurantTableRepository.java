package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    @Query("""
        SELECT t
        FROM RestaurantTable t
        WHERE (:capacity IS NULL OR t.capacity >= :capacity)
        AND (
            (
                :slot IS NOT NULL
                AND NOT EXISTS (
                    SELECT rt
                    FROM ReservationTable rt
                    WHERE rt.table = t
                      AND rt.reservation.reservationTime >= :startTime
                      AND rt.reservation.reservationTime < :endTime
                      AND rt.reservation.slot = :slot
                      AND rt.reservation.status IN :statuses
                )
            )
            OR
            (
                :slot IS NULL
                AND (
                    SELECT COUNT(DISTINCT rt.reservation.slot)
                    FROM ReservationTable rt
                    WHERE rt.table = t
                      AND rt.reservation.reservationTime >= :startTime
                      AND rt.reservation.reservationTime < :endTime
                      AND rt.reservation.status IN :statuses
                ) < :totalSlots
            )
        )
        ORDER BY t.tableNumber
        """)
    List<RestaurantTable> findAvailableTables(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("slot") Integer slot,
            @Param("capacity") Integer capacity,
            @Param("statuses") List<ReservationStatus> statuses,
            @Param("totalSlots") Integer totalSlots
    );
}
