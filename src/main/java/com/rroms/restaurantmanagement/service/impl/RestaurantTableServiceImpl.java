package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.enums.ReservationStatus;
import com.rroms.restaurantmanagement.repository.RestaurantTableRepository;
import com.rroms.restaurantmanagement.service.RestaurantTableService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RestaurantTableServiceImpl implements RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    public RestaurantTableServiceImpl(RestaurantTableRepository restaurantTableRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Override
    public List<RestaurantTable> findAvailableTables(LocalDate date, Integer slot, Integer capacity) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = startTime.plusDays(1);

        return restaurantTableRepository.findAvailableTables(
                startTime,
                endTime,
                slot,
                capacity,
                List.of(
                        ReservationStatus.PENDING,
                        ReservationStatus.CONFIRMED
                ),
                4
        );
    }
}
