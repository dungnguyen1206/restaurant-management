package com.rroms.restaurantmanagement.repository;

import com.rroms.restaurantmanagement.entity.ReservationTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTableRepository extends JpaRepository<ReservationTable, Long> {
}
