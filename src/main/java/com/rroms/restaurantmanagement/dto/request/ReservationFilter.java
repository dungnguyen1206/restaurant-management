package com.rroms.restaurantmanagement.dto.request;

import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationFilter {

    private ReservationStatus status;

    private String keyword;

    private Long tableId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reservationDate;
}
