package com.rroms.restaurantmanagement.service;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationService {
    Long countTodayReservation( LocalDateTime startDate, LocalDateTime endDate);
}
