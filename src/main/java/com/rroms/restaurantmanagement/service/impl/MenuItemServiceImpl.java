package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.DtoMapper.DtoMapper;
import com.rroms.restaurantmanagement.criteria.MenuItemCriteria;
import com.rroms.restaurantmanagement.dto.request.MenuItemDto;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.service.MenuItemService;
import com.rroms.restaurantmanagement.specfication.MenuItemSpecification;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final DtoMapper dtoMapper;

    public MenuItemServiceImpl(MenuItemRepository menuItemRepository, DtoMapper dtoMapper) {

        this.menuItemRepository = menuItemRepository;
        this.dtoMapper = dtoMapper;
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
