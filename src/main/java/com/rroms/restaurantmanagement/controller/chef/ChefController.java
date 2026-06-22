package com.rroms.restaurantmanagement.controller.chef;

import com.rroms.restaurantmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chef")
@RequiredArgsConstructor
public class ChefController {
    private final OrderService orderService;


}
