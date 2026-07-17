package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.ReservationResponseForManager;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import com.rroms.restaurantmanagement.repository.ReservationRepository;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ReservationServiceImpl implements ReservationService {


    private final ReservationRepository reservationRepository;

    @Override
    public Long countTodayReservation(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findTodayReservation(startDate, endDate);
    }

    @Override
    public Page<ReservationResponseForManager> getAllTodayReservationsForManager(LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer  size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationRepository.getAllTodayReservationsForManager(startDate,endDate, pageable).map(this::toReservationResponseForManager);
    }

    private ReservationResponseForManager toReservationResponseForManager(Reservation reservation) {
        return ReservationResponseForManager.builder()
                .id(reservation.getReservationId())
                .fullName(reservation.getFullName())
                .phoneNumber(reservation.getPhone())
                .tables(reservation.getReservationTables().stream().map(ReservationTable::getTable).collect(Collectors.toSet()))
                .reservationTime(reservation.getReservationTime())
                .status(reservation.getStatus().toString())
                .build();
    }
}
