package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Reservation, Long> {


}