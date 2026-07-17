package com.rroms.restaurantmanagement.repository.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public interface OrderListProjection {

    Long getOrderId();

    BigDecimal getTotalAmount();

    String getStatus();

    /*
     * Nhận dữ liệu datetime2 từ native query.
     * Hai tên này phải trùng alias:
     * createdAtUtc và updatedAtUtc.
     */
    LocalDateTime getCreatedAtUtc();

    LocalDateTime getUpdatedAtUtc();

    Long getCreatedBy();

    Long getUpdatedBy();

    Long getTableId();

    String getTableNumber();

    Long getUserId();

    String getUsername();

    Integer getTotalItems();

    /*
     * Chuyển LocalDateTime UTC thành Instant.
     */
    default Instant getCreatedAt() {
        LocalDateTime createdAtUtc = getCreatedAtUtc();

        if (createdAtUtc == null) {
            return null;
        }

        return createdAtUtc.toInstant(ZoneOffset.UTC);
    }

    default Instant getUpdatedAt() {
        LocalDateTime updatedAtUtc = getUpdatedAtUtc();

        if (updatedAtUtc == null) {
            return null;
        }

        return updatedAtUtc.toInstant(ZoneOffset.UTC);
    }

    /*
     * Hiển thị thời gian theo múi giờ Việt Nam.
     */
    default String getCreatedDateTime() {
        Instant createdAt = getCreatedAt();

        if (createdAt == null) {
            return "";
        }

        return createdAt
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}