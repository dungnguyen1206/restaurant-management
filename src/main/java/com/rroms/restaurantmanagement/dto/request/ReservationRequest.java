package com.rroms.restaurantmanagement.dto.request;

import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;

import java.time.LocalDateTime;
import lombok.*;

@Data
public class ReservationRequest {
    private Long reservationId;
    private LocalDateTime reservationTime;
    private Long userId;
    private String phone;
    private String fullName;
    private String note;
    private ReservationStatus status;
}
