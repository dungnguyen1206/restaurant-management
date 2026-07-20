package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.request.ReservationPaymentDTO;
import com.rroms.restaurantmanagement.dto.request.ReservationFilter;
import com.rroms.restaurantmanagement.dto.request.ReservationRequest;
import com.rroms.restaurantmanagement.dto.request.WalkInRequest;
import com.rroms.restaurantmanagement.dto.response.ReservationResponseForManager;
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
import com.rroms.restaurantmanagement.repository.ReservationTableRepository;
import com.rroms.restaurantmanagement.repository.RestaurantTableRepository;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    public static final BigDecimal DEPOSIT_PER_TABLE = new BigDecimal("200000");
    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(
            ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final PaymentRepository paymentRepository;
    private  final ReservationTableRepository reservationTableRepository;

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
        return reservationRepository.findByIdWithTables(id).orElseThrow(() -> new RuntimeException("Reservation không tồn tại"));
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
    @Transactional
    public void cancel(Long id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            throw new RuntimeException("Không thể hủy đặt bàn đã nhận khách");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }

        for (ReservationTable reservationTable : reservation.getReservationTables()) {
            RestaurantTable table = reservationTable.getTable();

            if (table != null && table.getStatus() == TableStatus.RESERVED) {
                table.setStatus(TableStatus.AVAILABLE);
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void createWalkIn(WalkInRequest request) {
        if (request.getTableIds() == null || request.getTableIds().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một bàn");
        }

        if (request.getNumberOfGuests() == null || request.getNumberOfGuests() <= 0) {
            throw new RuntimeException("Số lượng khách phải lớn hơn 0");
        }

        Set<Long> uniqueTableIds = new LinkedHashSet<>(request.getTableIds());
        if (uniqueTableIds.size() != request.getTableIds().size()) {
            throw new RuntimeException("Không được chọn trùng bàn");
        }

        List<RestaurantTable> tables = tableRepository.findAllById(uniqueTableIds);

        if (tables.size() != request.getTableIds().size()) {
            throw new RuntimeException("Một hoặc nhiều bàn không tồn tại");
        }

        int totalCapacity = 0;
        for (RestaurantTable table : tables) {
            if (table.getStatus() != TableStatus.AVAILABLE) {
                throw new RuntimeException("Chỉ có thể chọn bàn đang trống");
            }

            totalCapacity += table.getCapacity();
        }

        if (totalCapacity < request.getNumberOfGuests()) {
            throw new RuntimeException("Tổng sức chứa của các bàn đã chọn không đủ số lượng khách");
        }

        String fullName = buildFullName(
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName()
        );

        Reservation reservation = Reservation.builder()
                .fullName(fullName)
                .numberOfGuests(request.getNumberOfGuests())
                .note(request.getNote())
                .reservationTime(LocalDateTime.now())
                .status(ReservationStatus.CHECKED_IN)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        for (RestaurantTable table : tables) {
            ReservationTable reservationTable = ReservationTable.builder()
                    .reservation(savedReservation)
                    .table(table)
                    .build();

            reservationTableRepository.save(reservationTable);

            table.setStatus(TableStatus.OCCUPIED);
        }

        tableRepository.saveAll(tables);
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

        if (reservation.getReservationTables() != null && !reservation.getReservationTables().isEmpty()) {
            int totalCapacity = 0;

            for (ReservationTable reservationTable : reservation.getReservationTables()) {
                RestaurantTable selectedTable = reservationTable.getTable();

                if (selectedTable == null) {
                    throw new RuntimeException("Table khong ton tai");
                }

                if (selectedTable.getStatus() != TableStatus.AVAILABLE) {
                    throw new RuntimeException("Chi co the xac nhan ban dang trong");
                }

                totalCapacity += selectedTable.getCapacity();
            }

            if (reservation.getNumberOfGuests() != null
                    && totalCapacity < reservation.getNumberOfGuests()) {
                throw new RuntimeException("Tong suc chua cua ban khong du so luong khach");
            }

            for (ReservationTable reservationTable : reservation.getReservationTables()) {
                RestaurantTable selectedTable = reservationTable.getTable();
                selectedTable.setStatus(TableStatus.RESERVED);
                tableRepository.save(selectedTable);
            }

            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            return;
        }

        if (tableId == null) {
            throw new RuntimeException("Vui long chon ban");
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

    @Override
    public Long countTodayReservation(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findTodayReservation(startDate, endDate);
    }

    @Override
    public Page<ReservationResponseForManager> getAllTodayReservationsForManager(LocalDateTime startDate, LocalDateTime endDate, Integer page, Integer  size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationRepository.getAllTodayReservationsForManager(startDate,endDate, pageable).map(this::toReservationResponseForManager);
    }


    private ReservationResponseForManager toReservationResponseForManager(Reservation reservation) {
        return ReservationResponseForManager.builder()
                .id(reservation.getReservationId())
                .fullName(reservation.getFullName())
                .phoneNumber(reservation.getPhone())
                .tables(reservation.getReservationTables().stream().map(ReservationTable::getTable).collect(Collectors.toSet()))
                .reservationTime(reservation.getReservationTime())
                .status(reservation.getStatus().toString())
                .build();
    }


    @Override
    public Page<Reservation> getReservations(Long waiterId, ReservationFilter reservationRequest, Pageable pageable) {
         return reservationRepository.findAll(filter(waiterId, reservationRequest), pageable);
    }

    private Specification<Reservation> filter(Long waiterId, ReservationFilter reservation){
        return((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            ReservationFilter reservationRequest = reservation != null ? reservation : new ReservationFilter();

            query.distinct(true);

            Join<Reservation, ReservationTable> reservationTableJoin = root.join("reservationTables", JoinType.LEFT);

            Join<ReservationTable, RestaurantTable> restaurantTableJoin = reservationTableJoin.join("table", JoinType.LEFT);

            if (waiterId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                restaurantTableJoin.get("assignedWaiter").get("userId"),
                                waiterId
                        )
                );
            }

            if(reservationRequest.getStatus() != null){
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                ReservationStatus.CHECKED_IN
                        )
                );
            }

            if(reservationRequest.getKeyword() != null && !reservationRequest.getKeyword().isBlank()){
                String keyword = reservationRequest.getKeyword().trim().toLowerCase();

                Predicate fullName = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")),
                        "%" + keyword + "%"
                );

                Predicate phone = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phone")),
                        "%" + keyword + "%"
                );

                Predicate tableName = criteriaBuilder.like(
                        criteriaBuilder.lower(restaurantTableJoin.get("tableNumber")),
                        "%" + keyword + "%"
                );

                predicates.add(criteriaBuilder.or(fullName, phone, tableName));
            }

            if(reservationRequest.getReservationDate() != null){
                LocalDateTime start =reservationRequest.getReservationDate().atStartOfDay();

                LocalDateTime end =reservationRequest.getReservationDate().atTime(23,59,59);

                predicates.add(
                        criteriaBuilder.between(
                                root.get("reservationTime"),
                                start, end
                        )
                );
            }



            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

        });
    }
}

