package com.rroms.restaurantmanagement.service.impl;

import com.rroms.restaurantmanagement.dto.request.CreateMenuItemsRequest;
import com.rroms.restaurantmanagement.dto.response.MenuItemResponseForManager;
import com.rroms.restaurantmanagement.entity.Category;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.exception.DataConflictException;
import com.rroms.restaurantmanagement.repository.CategoryRepository;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.service.CloudinaryService;
import com.rroms.restaurantmanagement.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<MenuItemResponseForManager> getAllMenuItems(Long categoryId, String filterKey, int page, int size) {
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
                .image(menuItem.getImageUrl()).build();
    }


}
