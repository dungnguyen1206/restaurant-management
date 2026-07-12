package com.rroms.restaurantmanagement.repository.projection;

import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;

import java.time.LocalDateTime;

public interface ReservationProjection {
    Long getReservationId();

    String getFullName();

    String getPhone();

    LocalDateTime getReservationTime();

    ReservationStatus getStatus();

    Integer getNumberOfGuests();

}
