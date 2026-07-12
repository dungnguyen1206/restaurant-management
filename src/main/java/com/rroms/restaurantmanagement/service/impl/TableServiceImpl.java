package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.constant.TableArea;
import com.rroms.restaurantmanagement.repository.TableRepository;
import com.rroms.restaurantmanagement.repository.projection.TableView;
import com.rroms.restaurantmanagement.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;

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
}