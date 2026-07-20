package com.rroms.restaurantmanagement.controller.waiter;


import com.rroms.restaurantmanagement.dto.request.ReservationFilter;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/waiter")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/reservations")
    public String Reservations(@ModelAttribute ReservationFilter filter,
                               @PageableDefault(size = 5, sort = "reservationTime") Pageable pageable,
                               Model model,
                               @AuthenticationPrincipal(expression = "user") User user){
        Long id = user.getUserId();
        filter.setStatus(ReservationStatus.CHECKED_IN);
        model.addAttribute("filter", filter);
        model.addAttribute("page", reservationService.getReservations(id, filter, pageable));

        return "waiter/content/Reservations";
    }


}
