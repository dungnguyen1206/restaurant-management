package com.rroms.restaurantmanagement.dto.response;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ReservationResponseForManager {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private Set<RestaurantTable> tables;
    private LocalDateTime reservationTime;
    private String status;
}
