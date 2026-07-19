package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT r.reservationId FROM Reservation r
            WHERE r.user.userId = :userId
              AND (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<Long> findPageIdsByUserAndStatus(@Param("userId") Long userId,
                                          @Param("status") ReservationStatus status,
                                          Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM Reservation r
            LEFT JOIN FETCH r.reservationTables rt
            LEFT JOIN FETCH rt.table
            WHERE r.reservationId IN :ids
            """)
    List<Reservation> findAllByIdsWithTables(@Param("ids") Collection<Long> ids);

    @Query("""
            SELECT DISTINCT r FROM Reservation r
            LEFT JOIN FETCH r.reservationTables rt
            LEFT JOIN FETCH rt.table
            WHERE r.reservationId = :id AND r.user.userId = :userId
            """)
    Optional<Reservation> findOwnedById(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            SELECT COUNT(rt) FROM ReservationTable rt
            WHERE rt.table.tableId IN :tableIds
              AND rt.reservation.reservationTime >= :startTime
              AND rt.reservation.reservationTime < :endTime
              AND rt.reservation.status IN :statuses
            """)
    long countConflicts(@Param("tableIds") Collection<Long> tableIds,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("statuses") Collection<ReservationStatus> statuses);
}
