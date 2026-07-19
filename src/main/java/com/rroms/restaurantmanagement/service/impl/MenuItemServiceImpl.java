package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.DtoMapper.DtoMapper;
import com.rroms.restaurantmanagement.criteria.MenuItemCriteria;
import com.rroms.restaurantmanagement.dto.request.MenuItemDto;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.exception.ResourceNotFoundException;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.rroms.restaurantmanagement.specfication.MenuItemSpecification;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final DtoMapper dtoMapper;


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

    @Override
    @Transactional(readOnly = true)
    public List<MenuItem> findMostPopular(int limit) {
        return menuItemRepository.findMostPopular(OrderStatus.CANCELLED, PageRequest.of(0, limit));
    }

    @Override
    public Page<MenuItemDto> searchMenu(MenuItemCriteria criteriaMenuItem, int page, int size) {

//        if ("available".equals(status)) {
//            soldOut = false;
//        } else if ("soldout".equals(status)) {
//            soldOut = true;
//        }

        Sort sorting = Sort.unsorted();

        if ("priceAsc".equals(criteriaMenuItem.getSort())) {

            sorting = Sort.by("price").ascending();

        } else if ("priceDesc".equals(criteriaMenuItem.getSort())) {

            sorting = Sort.by("price").descending();

        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<MenuItem> menuItems = menuItemRepository.findAll(MenuItemSpecification.build(criteriaMenuItem), pageable);
        List<MenuItemDto> dtos = new ArrayList<>();

        for(MenuItem item : menuItems.getContent()){
            dtos.add(dtoMapper.toMenuItemDto(item));
        }

        return new PageImpl<>(
                dtos,
                menuItems.getPageable(),
                menuItems.getTotalElements()
        );


    }
}
