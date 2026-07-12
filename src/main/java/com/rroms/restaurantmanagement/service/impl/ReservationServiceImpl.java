package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.request.WalkInRequest;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import com.rroms.restaurantmanagement.repository.ReservationRepository;
import com.rroms.restaurantmanagement.repository.ReservationTableRepository;
import com.rroms.restaurantmanagement.repository.TableRepository;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;
import com.rroms.restaurantmanagement.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rroms.restaurantmanagement.dto.request.WalkInRequest;
@RequiredArgsConstructor
@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;
    private final ReservationTableRepository reservationTableRepository;
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
    @Transactional
    public void checkIn(Long id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("Không thể check in reservation đã hủy");
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);

        for (ReservationTable reservationTable : reservation.getReservationTables()) {
            RestaurantTable table = reservationTable.getTable();
            if (table != null) {
                table.setStatus(TableStatus.OCCUPIED);
            }
        }

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

    @Override
    @Transactional
    public void createWalkIn(WalkInRequest request) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("Table không tồn tại"));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Chỉ có thể check in bàn đang trống");
        }

        String fullName = buildFullName(
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName()
        );

        Reservation reservation = Reservation.builder()
                .fullName(fullName)
                .phone(request.getPhone())
                .numberOfGuests(request.getNumberOfGuests())
                .note(request.getNote())
                .reservationTime(LocalDateTime.now())
                .status(ReservationStatus.CHECKED_IN)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        ReservationTable reservationTable = ReservationTable.builder()
                .reservation(savedReservation)
                .table(table)
                .build();

        reservationTableRepository.save(reservationTable);

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);
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
    private String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder fullName = new StringBuilder();

        if (firstName != null && !firstName.isBlank()) {
            fullName.append(firstName.trim());
        }

        if (middleName != null && !middleName.isBlank()) {
            if (!fullName.isEmpty()) {
                fullName.append(" ");
            }
            fullName.append(middleName.trim());
        }

        if (lastName != null && !lastName.isBlank()) {
            if (!fullName.isEmpty()) {
                fullName.append(" ");
            }
            fullName.append(lastName.trim());
        }

        if (fullName.isEmpty()) {
            throw new RuntimeException("Tên khách không được để trống");
        }

        return fullName.toString();
    }


    @Override
    @Transactional
    public void confirm(Long reservationId, Long tableId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xác nhận reservation đang PENDING");
        }

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table không tồn tại"));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Chỉ có thể chọn bàn đang trống");
        }

        if (reservation.getNumberOfGuests() != null
                && table.getCapacity() < reservation.getNumberOfGuests()) {
            throw new RuntimeException("Bàn không đủ số lượng khách");
        }

        ReservationTable reservationTable = ReservationTable.builder()
                .reservation(reservation)
                .table(table)
                .build();

        reservationTableRepository.save(reservationTable);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        table.setStatus(TableStatus.RESERVED);

        reservationRepository.save(reservation);
        tableRepository.save(table);
    }

}
