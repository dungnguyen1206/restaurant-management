package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.repository.ReservationRepository;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;
import com.rroms.restaurantmanagement.service.ReservationService;

import java.util.List;
import lombok.*;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;

    @Override
    public List<ReservationProjection> getReservationList(String keyword, String status) {
        ReservationStatus reservationStatus = parseStatus(status);
        Long reservationId = parseReservationId(keyword);

        return reservationRepository.searchReservations(
                normalizeKeyword(keyword),
                reservationId,
                reservationStatus
        );
    }

    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Reservation không tồn tại"));
    }

    @Override
    public void checkIn(Long id) {
         Reservation reservation = getReservationById(id);

         if(reservation.getStatus() == ReservationStatus.CANCELLED){
             throw new RuntimeException("Không the check in reservation đã hủy");
         }

         reservation.setStatus(ReservationStatus.CHECKED_IN);
         reservationRepository.save(reservation);
    }

    @Override
    public void cancel(Long id) {
        Reservation reservation = getReservationById(id);

        if(reservation.getStatus() == ReservationStatus.CHECKED_IN){
            throw new RuntimeException("Không the check in reservation đã hủy");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);


    }

    private String normalizeKeyword(String keyword){
        if(keyword == null || keyword.isBlank()){
            return null;
        }
        return keyword.trim();
    }

    private ReservationStatus parseStatus(String status){
        if(status == null || status.isBlank()){
            return null;
        }
        return  ReservationStatus.valueOf(status);
    }

    private Long parseReservationId(String keyword){
        if (keyword == null || keyword.isBlank()){
            return null;
        }

        String normalized = keyword
                .trim()
                .toUpperCase()
                .toUpperCase()
                .replace("#RS-","")
                .replace("RS-","");

        if(!normalized.matches("\\d+")){
            return null;
        }
        return Long.valueOf(normalized);
    }
}
