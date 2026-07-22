package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.dto.request.AddOrderItemRequest;
import com.rroms.restaurantmanagement.dto.request.UpdateOrderItemRequest;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.OrderItem;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.OrderItemStatus;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.exception.ResourceNotFoundException;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.repository.OrderItemRepository;
import com.rroms.restaurantmanagement.repository.OrderRepository;
import com.rroms.restaurantmanagement.repository.ReservationRepository;
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

import java.math.BigDecimal;
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
    private final ReservationRepository reservationRepository;
    private final OrderItemRepository orderItemRepository;

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

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng đang ở trạng thái chờ xử lý.");
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalStateException("Đơn hàng #" + orderId + " không có món để xác nhận.");
        }

        Map<Long, Integer> requiredQuantities = new TreeMap<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getStatus() != OrderItemStatus.PENDING) {
                continue;
            }
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
        if (requiredQuantities.isEmpty()) {
            throw new IllegalStateException("Order khong co mon PENDING moi de xac nhan.");
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
            if (orderItem.getStatus() == OrderItemStatus.PENDING) {
                orderItem.setStatus(OrderItemStatus.PREPARING);
            }
        }
        order.setStatus(OrderStatus.PREPARING);
    }

    @Override
    @Transactional
    public void markKitchenOrderReady(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order khong ton tai"));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new RuntimeException("Chi order PREPARING moi duoc bao san sang");
        }
        boolean hasPendingItems = order.getOrderItems().stream()
                .anyMatch(item -> item.getStatus() == OrderItemStatus.PENDING);
        if (hasPendingItems) {
            throw new RuntimeException("Con mon PENDING moi, chef can xac nhan truoc khi bao san sang");
        }

        // Chef bao mon san sang: OrderItem READY, order van PREPARING cho toi khi waiter phuc vu.
        for (OrderItem item : order.getOrderItems()) {
            if (item.getStatus() == OrderItemStatus.PREPARING) {
                item.setStatus(OrderItemStatus.READY);
            }
        }
    }


    @Override
    @Transactional
    public void handleUpdateStatusOrder(Long orderId, OrderStatus orderStatus) {
        Optional<Order> orderOpt = this.orderRepository.findById(orderId);
        if(orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(orderStatus);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                if(orderItem.getStatus() == OrderItemStatus.PREPARING) {
                    orderItem.setStatus(OrderItemStatus.CANCELLED);
                    this.orderItemRepository.save(orderItem);
                }
            }
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
    public Order findById(Long orderId) {
        return this.orderRepository.findByIdWithDetails(orderId).orElse(null);
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

    @Override
    @Transactional(readOnly = true)
    public Page<OrderListProjection> getReceptionistOrderList(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        String searchKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchKeyword = keyword.trim();
        }

        String orderStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            orderStatus = status.trim();
        }

        return orderRepository.getReceptionistOrderList(searchKeyword, orderStatus, pageable);
    }

    private Reservation getCheckedInReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithTables(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation khong ton tai"));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new RuntimeException("Chi reservation CHECKED_IN moi duoc tao order");
        }

        return reservation;
    }


    ///waiter
    private Order createDraftOrder(Reservation reservation, User waiter) {
        RestaurantTable table = reservation.getReservationTables().stream()
                .findFirst()
                .map(ReservationTable::getTable)
                .orElse(null);

        return Order.builder()
                .reservation(reservation)
                .table(table)
                .user(waiter)
                .status(OrderStatus.PENDING)
                .submittedToKitchen(false)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();
    }

    private Order getDraftOrder(Long reservationId) {
        Order order = orderRepository.findActiveByReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("Chua co order cho reservation nay"));

        if (Boolean.TRUE.equals(order.getSubmittedToKitchen())) {
            throw new RuntimeException("Chi duoc sua gio hang khi order chua gui bep");
        }

        return order;
    }

    private OrderItem findPendingItem(Order order, Long itemId) {
        for (OrderItem item : order.getOrderItems()) {
            if (item.getStatus() == OrderItemStatus.PENDING
                    && item.getMenuItem() != null
                    && item.getMenuItem().getItemId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    private OrderItem findItemInOrder(Order order, Long orderItemId) {
        return order.getOrderItems().stream()
                .filter(item -> item.getOrderItemId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mon khong nam trong order nay"));
    }

    private void updateTotalAmount(Order order) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalAmount(total);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getActiveOrderForReservation(Long reservationId) {
        return orderRepository.findActiveByReservationId(reservationId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getWaiterOrders(Long waiterId, Pageable pageable) {
        return orderRepository.findWaiterOrders(waiterId, pageable);
    }

    @Override
    @Transactional
    public void addItemToDraftOrder(Long reservationId, AddOrderItemRequest request, User waiter) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("So luong mon phai lon hon 0");
        }

        Reservation reservation = getCheckedInReservation(reservationId);
        MenuItem menuItem = menuItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Mon khong ton tai"));

        int stock = menuItem.getVirtualInStock() == null ? 0 : menuItem.getVirtualInStock();
        if (Boolean.TRUE.equals(menuItem.getIsSoldOut()) || stock < request.getQuantity()) {
            throw new RuntimeException("Mon khong du so luong trong kho");
        }

        Order order = orderRepository.findByReservationIdWithDetails(reservationId)
                .orElseGet(() -> createDraftOrder(reservation, waiter));
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order nay da ket thuc, khong the them mon moi");
        }

        OrderItem existingItem = Boolean.TRUE.equals(order.getSubmittedToKitchen())
                ? null
                : findPendingItem(order, menuItem.getItemId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setSpecialNote(request.getNote());
        } else {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .specialNote(request.getNote())
                    .status(OrderItemStatus.PENDING)
                    .build();
            order.getOrderItems().add(orderItem);
        }

        if (order.getStatus() == OrderStatus.SERVED) {
            order.setStatus(OrderStatus.PENDING);
        }
        updateTotalAmount(order);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateDraftOrderItem(Long reservationId, UpdateOrderItemRequest request) {
        Order order = getDraftOrder(reservationId);
        OrderItem orderItem = findItemInOrder(order, request.getOrderItemId());

        if (orderItem.getStatus() != OrderItemStatus.PENDING) {
            throw new RuntimeException("Chi duoc sua mon dang PENDING");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("So luong mon phai lon hon 0");
        }

        orderItem.setQuantity(request.getQuantity());
        orderItem.setSpecialNote(request.getNote());
        updateTotalAmount(order);
    }

    @Override
    @Transactional
    public void removeDraftOrderItem(Long reservationId, Long orderItemId) {
        Order order = getDraftOrder(reservationId);
        OrderItem orderItem = findItemInOrder(order, orderItemId);

        if (orderItem.getStatus() != OrderItemStatus.PENDING) {
            throw new RuntimeException("Chi duoc xoa mon dang PENDING");
        }

        order.getOrderItems().remove(orderItem);
        orderItemRepository.delete(orderItem);
        updateTotalAmount(order);
    }

    @Override
    @Transactional
    public void sendOrderToKitchen(Long reservationId) {
        Order order = getDraftOrder(reservationId);

        if (order.getOrderItems().isEmpty()) {
            throw new RuntimeException("Gio hang chua co mon de gui bep");
        }

        order.setSubmittedToKitchen(true);
        for (OrderItem item : order.getOrderItems()) {
            item.setStatus(OrderItemStatus.PENDING);
        }
        updateTotalAmount(order);
    }

    @Override
    @Transactional
    public void markOrderItemServed(Long orderId, Long orderItemId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order khong ton tai"));
        OrderItem orderItem = findItemInOrder(order, orderItemId);

        if (orderItem.getStatus() != OrderItemStatus.READY) {
            throw new RuntimeException("Chi mon READY moi duoc phuc vu");
        }

        orderItem.setStatus(OrderItemStatus.SERVED);

        boolean allServed = order.getOrderItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.SERVED);
        if (allServed) {
            order.setStatus(OrderStatus.SERVED);
        }
    }

    @Override
    @Transactional

    public void markOrderServed(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order khong ton tai"));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new RuntimeException("Chi order PREPARING moi duoc phuc vu");
        }
        boolean allReady = order.getOrderItems().stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.READY);
        if (!allReady) {
            throw new RuntimeException("Chi duoc phuc vu khi tat ca mon da READY");
        }

        order.setStatus(OrderStatus.SERVED);
        for (OrderItem item : order.getOrderItems()) {
            if (item.getStatus() == OrderItemStatus.READY) {
                item.setStatus(OrderItemStatus.SERVED);
            }
        }
    }

}


