package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.request.CreateMenuItemsRequest;
import com.rroms.restaurantmanagement.dto.response.MenuItemResponseForManager;
import org.springframework.data.domain.Page;

public interface MenuItemService {
    Page<MenuItemResponseForManager> findAllMenuItems4Manager(Long categoryId, String filterKey , int page, int size);
    CreateMenuItemsRequest addNewMenuItem(CreateMenuItemsRequest createMenuItemsRequest);

    CreateMenuItemsRequest findMenuItemById(Long menuItemId);

    CreateMenuItemsRequest updateMenuItem(CreateMenuItemsRequest createMenuItemsRequest);
}
