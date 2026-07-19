package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.request.ReservationPaymentDTO;
import com.rroms.restaurantmanagement.entity.Payment;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.PaymentMethod;
import com.rroms.restaurantmanagement.entity.constant.PaymentStatus;
import com.rroms.restaurantmanagement.entity.constant.PaymentType;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import com.rroms.restaurantmanagement.repository.PaymentRepository;
import com.rroms.restaurantmanagement.repository.ReservationRepository;
import com.rroms.restaurantmanagement.repository.RestaurantTableRepository;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    public static final BigDecimal DEPOSIT_PER_TABLE = new BigDecimal("200000");
    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(
            ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> getSelectedAvailableTables(LocalDate date, List<Long> tableIds) {
        validateDateAndIds(date, tableIds);
        List<Long> distinctIds = tableIds.stream().distinct().toList();
        List<RestaurantTable> tables = tableRepository.findAllById(distinctIds);
        validateTables(date, distinctIds, tables);
        return tables.stream().sorted((a, b) -> a.getTableNumber().compareTo(b.getTableNumber())).toList();
    }

    @Override
    @Transactional
    public Reservation createPaidReservation(User user, ReservationPaymentDTO request) {
        validateDateAndIds(request.getDate(), request.getTableIds());
        List<Long> distinctIds = request.getTableIds().stream().distinct().sorted().toList();
        List<RestaurantTable> tables = tableRepository.findAllByIdForUpdate(distinctIds);
        validateTables(request.getDate(), distinctIds, tables);

        Reservation reservation = Reservation.builder()
                .status(ReservationStatus.PENDING)
                .reservationTime(request.getDate().atTime(12, 0))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .note(request.getNote() == null ? null : request.getNote().trim())
                .user(user)
                .reservationTables(new LinkedHashSet<>())
                .payments(new LinkedHashSet<>())
                .build();

        for (RestaurantTable table : tables) {
            reservation.getReservationTables().add(ReservationTable.builder()
                    .reservation(reservation).table(table).build());
        }
        Reservation saved = reservationRepository.save(reservation);

        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
                .amount(DEPOSIT_PER_TABLE.multiply(BigDecimal.valueOf(tables.size())))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PAID)
                .paidAt(now)
                .createdAt(now)
                .paymentType(PaymentType.DEPOSIT)
                .reservation(saved)
                .build();
        paymentRepository.save(payment);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> findMyReservations(User user, ReservationStatus status, int page, int size) {
        if (page < 0) page = 0;
        if (size < 1 || size > 20) size = 6;
        Page<Long> idPage = reservationRepository.findPageIdsByUserAndStatus(
                user.getUserId(), status, PageRequest.of(page, size));
        if (idPage.isEmpty()) {
            return new PageImpl<>(List.of(), idPage.getPageable(), idPage.getTotalElements());
        }
        Map<Long, Reservation> reservationsById = reservationRepository
                .findAllByIdsWithTables(idPage.getContent()).stream()
                .collect(Collectors.toMap(Reservation::getReservationId, Function.identity()));

        List<Reservation> orderedReservations = idPage.getContent().stream()
                .map(reservationsById::get)
                .filter(java.util.Objects::nonNull)
                .toList();
        return new PageImpl<>(orderedReservations, idPage.getPageable(), idPage.getTotalElements());
    }

    @Override
    @Transactional
    public void cancelPendingReservation(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findOwnedById(reservationId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch đặt bàn"));
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy lịch đặt bàn đang chờ xác nhận");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void deleteCancelledReservation(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findOwnedById(reservationId, user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch đặt bàn"));
        if (reservation.getStatus() != ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Chỉ có thể xóa vĩnh viễn lịch đặt bàn đã hủy");
        }
        reservationRepository.delete(reservation);
    }

    private void validateDateAndIds(LocalDate date, List<Long> tableIds) {
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Vui lòng chọn ngày hiện tại hoặc một ngày trong tương lai");
        }
        if (tableIds == null || tableIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một bàn");
        }
        if (tableIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Bàn được chọn không hợp lệ");
        }
    }

    private void validateTables(LocalDate date, List<Long> ids, List<RestaurantTable> tables) {
        if (tables.size() != ids.size()) {
            throw new IllegalArgumentException("Một hoặc nhiều bàn được chọn không tồn tại");
        }
        if (tables.stream().anyMatch(table -> table.getStatus() != TableStatus.AVAILABLE)) {
            throw new IllegalStateException("Một hoặc nhiều bàn được chọn đang ngừng phục vụ");
        }
        LocalDateTime start = date.atStartOfDay();
        if (reservationRepository.countConflicts(ids, start, start.plusDays(1), BLOCKING_STATUSES) > 0) {
            throw new IllegalStateException("Một hoặc nhiều bàn vừa được khách khác đặt. Vui lòng chọn lại");
        }
    }
}
