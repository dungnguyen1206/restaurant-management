package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
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
    public List<RestaurantTable> findAvailableTables(LocalDate date, Integer capacity) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        if (capacity != null && !List.of(2, 4, 6, 8).contains(capacity)) {
            throw new IllegalArgumentException("Sức chứa bàn không hợp lệ");
        }
        LocalDateTime start = selectedDate.atStartOfDay();
        return restaurantTableRepository.findAvailableTables(
                start, start.plusDays(1), TableStatus.AVAILABLE, capacity,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN));
    }
}
