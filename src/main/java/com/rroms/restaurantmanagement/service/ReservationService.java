package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;
import com.rroms.restaurantmanagement.dto.request.WalkInRequest;
import java.util.List;

public interface ReservationService {
    List<ReservationProjection> getReservationList(String keyword, String status);

    Reservation getReservationById(Long id);

    void checkIn(Long id);

    void cancel(Long id);


    void createWalkIn(WalkInRequest request);


    void confirm(Long reservationId, Long tableId);
}


