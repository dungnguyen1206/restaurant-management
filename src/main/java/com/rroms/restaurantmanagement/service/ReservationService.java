package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.response.ReservationResponseForManager;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    Long countTodayReservation( LocalDateTime startDate, LocalDateTime endDate);



    Page<ReservationResponseForManager> getAllTodayReservationsForManager(LocalDateTime startDate, LocalDateTime endDate, Integer pageNumber, Integer pageSize);
}
