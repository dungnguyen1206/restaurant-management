package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.repository.ReservationRepository;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class ReservationServiceImpl implements ReservationService {


    private final ReservationRepository reservationRepository;

    @Override
    public Long countTodayReservation(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findTodayReservation(startDate, endDate);
    }
}
