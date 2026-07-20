package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.response.OrderHistoryDTO;
import com.rroms.restaurantmanagement.dto.request.AddOrderItemRequest;
import com.rroms.restaurantmanagement.dto.request.UpdateOrderItemRequest;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.repository.projection.OrderListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import com.rroms.restaurantmanagement.dto.response.ChefDashboardDTO;

public interface OrderService {
    Order getActiveOrderForReservation(Long reservationId);
    Page<Order> getWaiterOrders(Long waiterId, Pageable pageable);
    void addItemToDraftOrder(Long reservationId, AddOrderItemRequest request, User waiter);
    void updateDraftOrderItem(Long reservationId, UpdateOrderItemRequest request);
    void removeDraftOrderItem(Long reservationId, Long orderItemId);
    void sendOrderToKitchen(Long reservationId);
    void markOrderItemServed(Long orderId, Long orderItemId);
    void markOrderServed(Long orderId);
    Page<Order> getKitchenOrders(String orderId, String status, Pageable pageable);
    void confirmKitchenOrder(Long orderId);
    void markKitchenOrderReady(Long orderId);
    void handleUpdateStatusOrder(Long orderId, OrderStatus orderStatus);
    Page<OrderHistoryDTO> searchChefOrderHistory(String keyword, LocalDate startDate, LocalDate endDate, String status, int page, int size);

    Page<OrderListProjection> getReceptionistOrderList(String keyword, String status, int page, int size);

    Order findById(Long orderId);

    ChefDashboardDTO getChefDashboardData();
}
