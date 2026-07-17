package com.rroms.restaurantmanagement.controller.manager;

import com.rroms.restaurantmanagement.dto.request.CreateMenuItemsRequest;
import com.rroms.restaurantmanagement.dto.response.CategoryResponseForManager;
import com.rroms.restaurantmanagement.dto.response.MenuItemResponseForManager;
import com.rroms.restaurantmanagement.service.CategoryService;
import com.rroms.restaurantmanagement.service.CloudinaryService;
import com.rroms.restaurantmanagement.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager/menu")
@RequiredArgsConstructor
public class ManagerMenuController {
    private final CategoryService categoryService;

    private final CloudinaryService cloudinaryService;
    private final MenuItemService menuItemService;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String filterKey,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "6") int size, Model model) {

        List<CategoryResponseForManager> categoryResponseForManagers = categoryService.findAll();

        model.addAttribute("categoryResponseForManagers", categoryResponseForManagers);

        Page<MenuItemResponseForManager> menuItemResponseForManagerList = menuItemService.getAllMenuItems(categoryId, filterKey, page, size);
        model.addAttribute("menuItemResponseForManagerList", menuItemResponseForManagerList.getContent());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("filterKey", filterKey);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", menuItemResponseForManagerList.getTotalPages());

        return "manager/menu/list";
    }

    @GetMapping("/create")
    private String create(Model model) {
        List<CategoryResponseForManager> categoryResponseForManagers = categoryService.findAll();
        model.addAttribute("categoryResponseForManagers", categoryResponseForManagers);
        model.addAttribute("createMenuItemsRequest", new CreateMenuItemsRequest());
        model.addAttribute("isUpdateMode", false);
        model.addAttribute("formAction", "/manager/menu/create");
        return "manager/menu/create";
    }

    @PostMapping("/create")
    private String create(@Valid @ModelAttribute CreateMenuItemsRequest createMenuItemsRequest, BindingResult bindingResult,
                          @RequestParam(value = "itemImage", required = false) MultipartFile file,
                          RedirectAttributes redirectAttributes, Model model) {
        List<CategoryResponseForManager> categoryResponseForManagers = categoryService.findAll();
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryResponseForManagers", categoryResponseForManagers);
            return "manager/menu/create";
        }

        if(file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file);
            createMenuItemsRequest.setItemImageUrl(imageUrl);
        }
        menuItemService.addNewMenuItem(createMenuItemsRequest);
        redirectAttributes.addFlashAttribute("createMenuItemsRequest", createMenuItemsRequest);
        redirectAttributes.addFlashAttribute("categoryResponseForManagers", categoryResponseForManagers);
        redirectAttributes.addFlashAttribute("successMessage","Món ăn được thêm vào hệ thống thành công");
        return "redirect:/manager/menu/create";
    }


    @GetMapping("/update")
    private String update(@RequestParam Long menuItemId, Model model) {
        CreateMenuItemsRequest updateMenuItemsRequest = menuItemService.findMenuItemById(menuItemId);
        List<CategoryResponseForManager> categoryResponseForManagers = categoryService.findAll();
        model.addAttribute("categoryResponseForManagers", categoryResponseForManagers);
        model.addAttribute("createMenuItemsRequest", updateMenuItemsRequest);
        model.addAttribute("isUpdateMode", true);
        model.addAttribute("formAction", "/manager/menu/update");
        return  "manager/menu/create";
    }

    @PostMapping("/update")
    private String update(RedirectAttributes redirectAttributes,
                          @Valid @ModelAttribute CreateMenuItemsRequest createMenuItemsRequest,
                          BindingResult bindingResult,
                          @RequestParam(value = "itemImage", required = false) MultipartFile file,
                          Model model) {

        List<CategoryResponseForManager> categoryResponseForManagers = categoryService.findAll();

        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryResponseForManagers", categoryResponseForManagers);
            model.addAttribute("createMenuItemsRequest", createMenuItemsRequest);
            model.addAttribute("isUpdateMode", true);
            model.addAttribute("formAction", "/manager/menu/update");
            return "manager/menu/create";
        }

        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file);
            createMenuItemsRequest.setItemImageUrl(imageUrl);
        }

        menuItemService.updateMenuItem(createMenuItemsRequest);

        redirectAttributes.addFlashAttribute("successMessage", "Món ăn cập nhật thành công");

        return "redirect:/manager/menu/update?menuItemId=" + createMenuItemsRequest.getItemId();
    }
}
