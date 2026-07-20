package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.response.CheckoutItemDTO;
import com.rroms.restaurantmanagement.dto.response.*;
import com.rroms.restaurantmanagement.entity.Invoice;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.OrderItem;
import com.rroms.restaurantmanagement.entity.Payment;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.constant.InvoiceStatus;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.entity.constant.PaymentMethod;
import com.rroms.restaurantmanagement.entity.constant.PaymentStatus;
import com.rroms.restaurantmanagement.entity.constant.PaymentType;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import com.rroms.restaurantmanagement.repository.InvoiceRepository;
import com.rroms.restaurantmanagement.repository.OrderRepository;
import com.rroms.restaurantmanagement.repository.PaymentRepository;
import com.rroms.restaurantmanagement.repository.TableRepository;
import com.rroms.restaurantmanagement.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private static final String VIETQR_BANK_BIN = "970418";
    private static final String VIETQR_ACCOUNT_NO = "8830016176";
    private static final String VIETQR_ACCOUNT_NAME = "PHAM DUY KHANH";

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final TableRepository tableRepository;

    @Override
    @Transactional(readOnly = true)
    public CheckoutViewDTO getCheckoutView(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.SERVED) {
            throw new RuntimeException("Chỉ có thể checkout đơn đã SERVED");
        }

        BigDecimal totalAmount = calculateTotal(order);
        String transferContent = "ORDER-" + order.getOrderId();
        String vietQrUrl = buildVietQrUrl(totalAmount, transferContent);

        return new CheckoutViewDTO(
                order.getOrderId(),
                order.getTable() != null ? order.getTable().getTableNumber() : "",
                order.getUser() != null ? order.getUser().getUsername() : "",
                totalAmount,
                transferContent,
                vietQrUrl,
                buildItems(order)
        );
    }

    @Override
    @Transactional
    public void confirmPaid(Long orderId, PaymentMethod paymentMethod) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.SERVED) {
            throw new RuntimeException("Chỉ có thể checkout đơn đã SERVED");
        }

        BigDecimal totalAmount = calculateTotal(order);

        Invoice invoice = invoiceRepository.findByOrderOrderId(orderId)
                .orElseGet(() -> Invoice.builder()
                        .order(order)
                        .createdAt(Instant.now())
                        .build());

        invoice.setTotal(totalAmount);
        invoice.setDeposit(BigDecimal.ZERO);
        invoice.setFinalAmount(totalAmount);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        Payment payment = Payment.builder()
                .amount(totalAmount)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .invoice(savedInvoice)
                .paymentType(PaymentType.FINAL_PAYMENT)
                .build();

        paymentRepository.save(payment);

        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.COMPLETED);
        if (order.getReservation() != null) {
            order.getReservation().setStatus(ReservationStatus.COMPLETED);
        }
        orderRepository.save(order);

        List<RestaurantTable> tables = getCheckoutTables(order);
        for (RestaurantTable table : tables) {
            table.setStatus(TableStatus.AVAILABLE);
            table.setAssignedWaiter(null);
        }
        tableRepository.saveAll(tables);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));
    }

    private BigDecimal calculateTotal(Order order) {
        if (order.getTotalAmount() != null) {
            return order.getTotalAmount();
        }

        BigDecimal total = BigDecimal.ZERO;

        if (order.getOrderItems() == null) {
            return total;
        }

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        return total;
    }

    private List<CheckoutItemDTO> buildItems(Order order) {
        List<CheckoutItemDTO> items = new ArrayList<>();

        if (order.getOrderItems() == null) {
            return items;
        }

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            String itemName = "";
            if (item.getMenuItem() != null) {
                itemName = item.getMenuItem().getItemName();
            }

            items.add(new CheckoutItemDTO(
                    itemName,
                    quantity,
                    unitPrice,
                    lineTotal
            ));
        }

        return items;
    }

    private List<RestaurantTable> getCheckoutTables(Order order) {
        Set<RestaurantTable> tables = new LinkedHashSet<>();

        if (order.getReservation() != null && order.getReservation().getReservationTables() != null) {
            for (ReservationTable reservationTable : order.getReservation().getReservationTables()) {
                if (reservationTable.getTable() != null) {
                    tables.add(reservationTable.getTable());
                }
            }
        }

        if (order.getTable() != null) {
            tables.add(order.getTable());
        }

        return new ArrayList<>(tables);
    }

    private String buildVietQrUrl(BigDecimal amount, String transferContent) {
        String encodedContent = UriUtils.encode(transferContent, StandardCharsets.UTF_8);
        String encodedAccountName = UriUtils.encode(VIETQR_ACCOUNT_NAME, StandardCharsets.UTF_8);

        return "https://img.vietqr.io/image/"
                + VIETQR_BANK_BIN
                + "-"
                + VIETQR_ACCOUNT_NO
                + "-compact2.png"
                + "?amount="
                + amount.toBigInteger()
                + "&addInfo="
                + encodedContent
                + "&accountName="
                + encodedAccountName;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }

        return authentication.getName();
    }
}
