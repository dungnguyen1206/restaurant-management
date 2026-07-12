package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;

import java.util.List;

public interface ReservationService {
    List<ReservationProjection> getReservationList(String keyword, String status);

    Reservation getReservationById(Long id);

    void checkIn(Long id);

    void cancel(Long id);

}
