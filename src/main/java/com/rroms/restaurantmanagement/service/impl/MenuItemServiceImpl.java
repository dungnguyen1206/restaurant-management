package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.exception.ResourceNotFoundException;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<MenuItem> getAllMenuItems(String name, Long categoryId, Pageable pageable) {
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasCategory = categoryId != null;

        if (hasName && hasCategory) {
            return menuItemRepository.findByNameAndCategory(name.trim(), categoryId, pageable);
        } else if (hasName) {
            return menuItemRepository.findByName(name.trim(), pageable);
        } else if (hasCategory) {
            return menuItemRepository.findByCategory(categoryId, pageable);
        } else {
            return menuItemRepository.findAllWithCategory(pageable);
        }
    }

    @Override
    @Transactional
    public void updateVirtualStock(Long itemId, Integer virtualInStock) {
        if (virtualInStock == null || virtualInStock < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho phải lớn hơn hoặc bằng 0.");
        }

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn có ID " + itemId + "."));

        item.setVirtualInStock(virtualInStock);
        item.setIsSoldOut(virtualInStock == 0);
    }
}
