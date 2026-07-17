package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.reservationTime >= :startDate AND r.reservationTime < :endDate")
    Long findTodayReservation(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    @Query(
        value = "SELECT r FROM Reservation r LEFT JOIN FETCH r.reservationTables rt LEFT JOIN FETCH rt.table t WHERE r.reservationTime >= :startDate AND r.reservationTime < :endDate",
        countQuery = "SELECT COUNT(r) FROM Reservation r WHERE r.reservationTime >= :startDate AND r.reservationTime < :endDate"
    )
    Page<Reservation> getAllTodayReservationsForManager(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("""
            SELECT
                r.reservationId AS reservationId,
                r.fullName AS fullName,
                r.phone AS phone,
                r.reservationTime AS reservationTime,
                r.status AS status,
                r.numberOfGuests AS numberOfGuests
            FROM Reservation r
            WHERE
                (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(r.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR r.phone LIKE CONCAT('%', :keyword, '%')
                    OR (:reservationId IS NOT NULL AND r.reservationId = :reservationId)
                )
            AND
                (
                    :status IS NULL
                    OR r.status = :status
                )
            ORDER BY r.reservationTime DESC
            """)
    List<ReservationProjection> searchReservations(
            @Param("keyword") String keyword,
            @Param("reservationId") Long reservationId,
            @Param("status") ReservationStatus status
    );
}
