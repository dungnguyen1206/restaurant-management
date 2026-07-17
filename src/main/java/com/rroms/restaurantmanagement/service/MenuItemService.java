package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.request.CreateMenuItemsRequest;
import com.rroms.restaurantmanagement.dto.response.MenuItemResponseForManager;
import org.springframework.data.domain.Page;
import com.rroms.restaurantmanagement.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuItemService {
    Page<MenuItemResponseForManager> findAllMenuItems4Manager(Long categoryId, String filterKey , int page, int size);
    CreateMenuItemsRequest addNewMenuItem(CreateMenuItemsRequest createMenuItemsRequest);

    CreateMenuItemsRequest findMenuItemById(Long menuItemId);

    CreateMenuItemsRequest updateMenuItem(CreateMenuItemsRequest createMenuItemsRequest);
    Page<MenuItem> getAllMenuItems(String name, Long categoryId, Pageable pageable);
    void updateVirtualStock(Long itemId, Integer virtualInStock);
}
