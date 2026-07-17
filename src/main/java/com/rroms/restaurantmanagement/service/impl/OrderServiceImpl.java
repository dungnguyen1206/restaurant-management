package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.OrderItem;
import com.rroms.restaurantmanagement.entity.constant.OrderItemStatus;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.exception.ResourceNotFoundException;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.repository.OrderRepository;
import com.rroms.restaurantmanagement.repository.projection.OrderListProjection;
import com.rroms.restaurantmanagement.service.OrderService;
import com.rroms.restaurantmanagement.dto.response.ChefDashboardDTO;
import com.rroms.restaurantmanagement.dto.response.PopularMenuItemDTO;
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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public Page<Order> getKitchenOrders(String orderId, String status, Pageable pageable) {
        List<OrderStatus> validStatus = new ArrayList<>();
        if (status == null || status.isEmpty()) {
            validStatus = List.of(OrderStatus.PENDING, OrderStatus.PREPARING);
        } else {
            validStatus.add(OrderStatus.valueOf(status));
        }

        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime startOfDay = ZonedDateTime.now(vietnamZone).toLocalDate().atStartOfDay(vietnamZone);
        ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        Instant start = startOfDay.toInstant();
        Instant end = endOfDay.toInstant();

        Long parsedOrderId = null;
        if (orderId != null && !orderId.trim().isEmpty()) {
            try {
                parsedOrderId = Long.parseLong(orderId.trim());
            } catch (NumberFormatException e) {
                parsedOrderId = -1L; // Invalid search value, will yield empty result safely
            }
        }
        if (parsedOrderId != null) {
            return orderRepository.getKitchenOrdersWithId(validStatus, start, end, parsedOrderId, pageable);
        } else {
            return orderRepository.getKitchenOrdersWithoutId(validStatus, start, end, pageable);
        }
    }

    @Override
    @Transactional
    public void confirmKitchenOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng #" + orderId + "."));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng đang ở trạng thái chờ xử lý.");
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalStateException("Đơn hàng #" + orderId + " không có món để xác nhận.");
        }

        Map<Long, Integer> requiredQuantities = new TreeMap<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getMenuItem() == null || orderItem.getMenuItem().getItemId() == null) {
                throw new IllegalStateException("Đơn hàng có món ăn không còn tồn tại trong thực đơn.");
            }
            if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
                throw new IllegalStateException("Số lượng món trong đơn hàng không hợp lệ.");
            }
            requiredQuantities.merge(
                    orderItem.getMenuItem().getItemId(),
                    orderItem.getQuantity(),
                    Math::addExact
            );
        }

        List<MenuItem> lockedMenuItems = menuItemRepository.findAllByIdForUpdate(requiredQuantities.keySet());
        Map<Long, MenuItem> menuItemsById = lockedMenuItems.stream()
                .collect(Collectors.toMap(MenuItem::getItemId, Function.identity()));

        if (menuItemsById.size() != requiredQuantities.size()) {
            throw new IllegalStateException("Một hoặc nhiều món trong đơn không còn tồn tại trong thực đơn.");
        }

        List<String> shortages = new ArrayList<>();
        for (Map.Entry<Long, Integer> requirement : requiredQuantities.entrySet()) {
            MenuItem menuItem = menuItemsById.get(requirement.getKey());
            int available = menuItem.getVirtualInStock() == null ? 0 : menuItem.getVirtualInStock();
            if (available < requirement.getValue()) {
                shortages.add(menuItem.getItemName()
                        + " (cần " + requirement.getValue() + ", còn " + available + ")");
            }
        }

        if (!shortages.isEmpty()) {
            throw new IllegalStateException("Không đủ tồn kho: " + String.join(", ", shortages) + ".");
        }

        for (Map.Entry<Long, Integer> requirement : requiredQuantities.entrySet()) {
            MenuItem menuItem = menuItemsById.get(requirement.getKey());
            int remainingStock = menuItem.getVirtualInStock() - requirement.getValue();
            menuItem.setVirtualInStock(remainingStock);
            menuItem.setIsSoldOut(remainingStock == 0);
        }
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItem.setStatus(OrderItemStatus.PREPARING);
        }
        order.setStatus(OrderStatus.PREPARING);
    }


    @Override
    @Transactional
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

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListProjection> getReceptionistOrderList(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        String searchKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchKeyword = keyword.trim();
        }
    @Override
    public Order findById(Long orderId) {
        return this.orderRepository.findByIdWithItems(orderId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ChefDashboardDTO getChefDashboardData() {
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime startOfDay = ZonedDateTime.now(vietnamZone).toLocalDate().atStartOfDay(vietnamZone);
        ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        Instant start = startOfDay.toInstant();
        Instant end = endOfDay.toInstant();

        long pendingCount = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PENDING, start, end);
        long preparingCount = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PREPARING, start, end);

        List<Order> completedOrders = orderRepository.findByStatusAndCreatedAtBetween(OrderStatus.COMPLETED, start, end);
        long completedCountToday = completedOrders.size();

        String orderStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            orderStatus = status.trim();
        }

        return orderRepository.getReceptionistOrderList(searchKeyword, orderStatus, pageable);
    }

        long totalSeconds = 0;
        int validDurationCount = 0;
        for (Order o : completedOrders) {
            if (o.getUpdatedAt() != null && o.getCreatedAt() != null) {
                totalSeconds += Duration.between(o.getCreatedAt(), o.getUpdatedAt()).getSeconds();
                validDurationCount++;
            }
        }
        String avgTimeStr = "00:00";
        if (validDurationCount > 0) {
            long avgSeconds = totalSeconds / validDurationCount;
            long minutes = avgSeconds / 60;
            long seconds = avgSeconds % 60;
            avgTimeStr = String.format("%02d:%02d", minutes, seconds);
        }

        List<Object[]> rawPopular = orderRepository.getTopMenuItemsToday(
                OrderStatus.CANCELLED,
                start,
                end,
                PageRequest.of(0, 5)
        );
        List<PopularMenuItemDTO> popularItems = new ArrayList<>();
        for (Object[] row : rawPopular) {
            String itemName = (String) row[0];
            long totalOrdered = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            String imageUrl = (String) row[2];
            popularItems.add(new PopularMenuItemDTO(itemName, totalOrdered, imageUrl));
        }

        List<Order> latestOrders = orderRepository.findLatestOrdersToday(
                start,
                end,
                PageRequest.of(0, 5)
        );
        return ChefDashboardDTO.builder()
                .pendingCount(pendingCount)
                .preparingCount(preparingCount)
                .completedCountToday(completedCountToday)
                .avgCookingTimeToday(avgTimeStr)
                .popularItems(popularItems)
                .latestOrders(latestOrders)
                .build();
    }
}
