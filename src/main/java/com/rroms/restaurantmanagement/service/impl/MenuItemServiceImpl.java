package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.request.CreateMenuItemsRequest;
import com.rroms.restaurantmanagement.dto.response.MenuItemResponseForManager;
import com.rroms.restaurantmanagement.entity.Category;
import com.rroms.restaurantmanagement.DtoMapper.DtoMapper;
import com.rroms.restaurantmanagement.criteria.MenuItemCriteria;
import com.rroms.restaurantmanagement.dto.request.MenuItemDto;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.exception.DataConflictException;
import com.rroms.restaurantmanagement.exception.ResourceNotFoundException;
import com.rroms.restaurantmanagement.repository.CategoryRepository;
import com.rroms.restaurantmanagement.exception.ResourceNotFoundException;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.service.CloudinaryService;
import com.rroms.restaurantmanagement.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.rroms.restaurantmanagement.specfication.MenuItemSpecification;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final DtoMapper dtoMapper;

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<MenuItemResponseForManager> findAllMenuItems4Manager(Long categoryId, String filterKey, int page, int size) {
        if (filterKey != null && filterKey.trim().isEmpty()) {
            filterKey = null;
        }
        String searchKey = null;
        if (filterKey != null && !filterKey.trim().isEmpty()) {
            searchKey = "%" + filterKey.toLowerCase(Locale.ROOT) + "%";
        }
        Pageable pageable = PageRequest.of(page, size);
        return menuItemRepository.findAllMenuItems(searchKey, categoryId, pageable).map(this::toMenuItemResponseForManager);
    }

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
    public CreateMenuItemsRequest addNewMenuItem(CreateMenuItemsRequest createMenuItemsRequest) {
        if (menuItemRepository.existsByItemNameIgnoreCase(createMenuItemsRequest.getItemName())) {
            throw new DataConflictException("Món ăn " + createMenuItemsRequest.getItemName() + " đã tồn tại");
        }
        Category category = categoryRepository.findById(createMenuItemsRequest.getCategoryId()).orElse(null);
        MenuItem result = MenuItem.builder()
                .itemName(createMenuItemsRequest.getItemName())
                .price(createMenuItemsRequest.getItemPrice())
                .description(createMenuItemsRequest.getItemDescription())
                .imageUrl(createMenuItemsRequest.getItemImageUrl())
                .category(category)
                .isSoldOut(false)
                .virtualInStock(0)
                .build();

        menuItemRepository.save(result);
        return createMenuItemsRequest;
    }

    @Override
    public CreateMenuItemsRequest findMenuItemById(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
        return toCreateMenuItemsRequest(menuItem);
    }

    @Override
    @Transactional
    public CreateMenuItemsRequest updateMenuItem(CreateMenuItemsRequest createMenuItemsRequest) {
        MenuItem menuItem = menuItemRepository.findMenuItemByItemId(createMenuItemsRequest.getItemId()).orElseThrow(() -> new DataConflictException("Món ăn không tồn tại!"));
        Category category = categoryRepository.findById(createMenuItemsRequest.getCategoryId()).orElseThrow(() -> new RuntimeException("Loại món ăn không tồn tại"));
        menuItem.setItemName(createMenuItemsRequest.getItemName());
        menuItem.setPrice(createMenuItemsRequest.getItemPrice());
        menuItem.setDescription(createMenuItemsRequest.getItemDescription());
        String newImageUrl = createMenuItemsRequest.getItemImageUrl();

        if (newImageUrl != null && !newImageUrl.trim().isEmpty()) {
            if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().trim().isEmpty()) {
                cloudinaryService.deleteImage(menuItem.getImageUrl());
            }
            menuItem.setImageUrl(newImageUrl);
        }
        menuItem.setCategory(category);
        menuItemRepository.save(menuItem);
        return toCreateMenuItemsRequest(menuItem);
    }

    private CreateMenuItemsRequest toCreateMenuItemsRequest(MenuItem menuItem) {
        return CreateMenuItemsRequest.builder()
                .itemId(menuItem.getItemId())
                .itemName(menuItem.getItemName())
                .itemDescription(menuItem.getDescription())
                .itemPrice(menuItem.getPrice())
                .itemImageUrl(menuItem.getImageUrl())
                .categoryId(menuItem.getCategory().getCategoryId()).build();
    }

    private MenuItemResponseForManager toMenuItemResponseForManager(MenuItem menuItem) {
        return MenuItemResponseForManager.builder()
                .id(menuItem.getItemId())
                .itemName(menuItem.getItemName())
                .itemDescription(menuItem.getDescription())
                .itemPrice(menuItem.getPrice())
                .image(menuItem.getImageUrl()).build();}
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
