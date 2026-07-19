package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.RoleName;
import com.rroms.restaurantmanagement.entity.constant.TableArea;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import com.rroms.restaurantmanagement.repository.TableRepository;
import com.rroms.restaurantmanagement.repository.UserRepository;
import com.rroms.restaurantmanagement.repository.projection.TableView;
import com.rroms.restaurantmanagement.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    @Override
    public List<TableView> filterTables(String keyword, String area, String capacity) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        TableArea areaEnum = null;
        if (area != null && !area.isBlank()) {
            try {
                areaEnum = TableArea.valueOf(area.toUpperCase());
            } catch (IllegalArgumentException ex) {
                System.out.println(">>> area không hợp lệ: " + area);
            }
        }

        Integer cap = null;
        if (capacity != null && !capacity.isBlank()) {
            try {
                cap = Integer.parseInt(capacity);
            } catch (NumberFormatException ex) {
                System.out.println(">>> capacity không hợp lệ: " + capacity);
            }
        }

        return tableRepository.filterTable(
                kw,
                areaEnum != null ? areaEnum.name() : null,
                cap
        );
    }

    @Override
    public RestaurantTable getTableById(Long tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table không tồn tại"));
    }

    @Override
    public List<User> getWaiters() {
        return userRepository.findByRoleName(RoleName.WAITER);
    }

    @Override
    @Transactional
    public void assignWaiter(Long tableId, Long waiterId) {
        RestaurantTable table = getTableById(tableId);

        if (table.getStatus() != TableStatus.RESERVED && table.getStatus() != TableStatus.OCCUPIED) {
            throw new RuntimeException("Chỉ gán waiter cho bàn đã reserved hoặc occupied");
        }

        User waiter = userRepository.findById(waiterId)
                .orElseThrow(() -> new RuntimeException("Waiter không tồn tại"));

        if (waiter.getRole() == null || waiter.getRole().getRoleName() != RoleName.WAITER) {
            throw new RuntimeException("User được chọn không phải waiter");
        }

        table.setAssignedWaiter(waiter);
        tableRepository.save(table);
    }

    @Override
    public List<RestaurantTable> getTablesByWaiter(Long waiterId) {
        return tableRepository.findByAssignedWaiterUserIdOrderByTableNumberAsc(waiterId);
    }
}