package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.repository.OrderRepository;
import com.rroms.restaurantmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public Page<Order> getKitchenOrders(String orderId, String status, Pageable pageable) {
        List<OrderStatus> validStatus = new ArrayList<>();
        if(status == null || status.isEmpty() || status.equals("")) {
            validStatus = List.of(OrderStatus.PENDING, OrderStatus.PREPARING);
        } else {
            validStatus.add(OrderStatus.valueOf(status));
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        Specification<Order> spec = hasStatus(validStatus).and(createdToday());
        if(orderId != null && !orderId.isEmpty()) {
            spec = spec.and(hasOrderId(Long.parseLong(orderId)));
        }
        return orderRepository.findAll(spec, pageable);
    }

    private Specification<Order> hasStatus(List<OrderStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    private Specification<Order> hasOrderId(Long orderId) {
        return (root, query, cb) -> cb.equal(root.get("orderId"), orderId);
    }

    private Specification<Order> createdToday() {
        return (root, query, cb) -> {
            LocalDateTime start = LocalDate.now().atStartOfDay();
            LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
            return cb.between(root.get("createdAt"), start, end);
        };
    }


    @Override
    public void handleUpdateStatusOrder(Long orderId, OrderStatus orderStatus) {
        Optional<Order> orderOpt = this.orderRepository.findById(orderId);
        if(orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(orderStatus);
            this.orderRepository.save(order);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderHistoryDTO> searchChefOrderHistory(
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Instant startInstant = null;
        if (startDate != null) {
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        Instant endInstant = null;
        if (endDate != null) {
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        }
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        OrderStatus orderStatus = null;
        if(status != null) {
            try {
                orderStatus = OrderStatus.valueOf(status.toString());
            } catch (IllegalArgumentException e) {
                orderStatus = null;
            }
        }
        return orderRepository.getOrderHistoryByUserId(searchKeyword, startInstant, endInstant, orderStatus, pageable);
    }


}

