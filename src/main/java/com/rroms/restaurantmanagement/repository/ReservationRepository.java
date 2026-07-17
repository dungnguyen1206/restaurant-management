package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {


    @Query("SELECT COUNT (*)  from  Reservation r where r.reservationTime >= :startDate and r.reservationTime <:endDate")
    Long findTodayReservation(@Param("startDate")LocalDateTime startDate, @Param("endDate")LocalDateTime endDate);
}
