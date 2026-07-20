package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.request.ReservationFilter;
import com.rroms.restaurantmanagement.dto.request.ReservationRequest;
import com.rroms.restaurantmanagement.dto.request.ReservationPaymentDTO;
import com.rroms.restaurantmanagement.dto.request.WalkInRequest;
import com.rroms.restaurantmanagement.dto.response.ReservationResponseForManager;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    List<RestaurantTable> getSelectedAvailableTables(LocalDate date, List<Long> tableIds);

    Reservation createPaidReservation(User user, ReservationPaymentDTO request);

    Page<Reservation> findMyReservations(User user, ReservationStatus status, int page, int size);

    void cancelPendingReservation(Long reservationId, User user);

    void deleteCancelledReservation(Long reservationId, User user);

    List<ReservationProjection> getReservationList(String keyword, String status);

    Reservation getReservationById(Long id);

    void checkIn(Long id);

    void cancel(Long id);


    void createWalkIn(WalkInRequest request);


    void confirm(Long reservationId, Long tableId);
    Long countTodayReservation(LocalDateTime startDate, LocalDateTime endDate);



    Page<ReservationResponseForManager> getAllTodayReservationsForManager(LocalDateTime startDate, LocalDateTime endDate, Integer pageNumber, Integer pageSize);

    Page<Reservation> getReservations(Long waiterId, ReservationFilter reservationRequest, Pageable pageable);
}


