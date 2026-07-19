package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.request.ReservationPaymentDTO;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.User;

import java.time.LocalDate;
import java.util.List;

import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import org.springframework.data.domain.Page;

public interface ReservationService {
    List<RestaurantTable> getSelectedAvailableTables(LocalDate date, List<Long> tableIds);

    Reservation createPaidReservation(User user, ReservationPaymentDTO request);

    Page<Reservation> findMyReservations(User user, ReservationStatus status, int page, int size);

    void cancelPendingReservation(Long reservationId, User user);

    void deleteCancelledReservation(Long reservationId, User user);
}
