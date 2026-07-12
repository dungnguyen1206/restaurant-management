package com.rroms.restaurantmanagement.controller.receptionist;

import com.rroms.restaurantmanagement.dto.request.ReservationRequest;
import com.rroms.restaurantmanagement.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import lombok.*;

@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class ReceptionistReservation {

    private final ReservationService reservationService;
    @GetMapping
    public String listReservation(Model model,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String status) {
        model.addAttribute("reservations", reservationService.getReservationList(keyword,status));

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "receptionist/reservations";
    }

    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable Long id){
        reservationService.checkIn(id);
        return "redirect:/receptionist/reservation";
    }

    @PostMapping("/{id}/cancel")
    public String checkOut(@PathVariable Long id){
        reservationService.cancel(id);
        return "redirect:/receptionist/reservation";
    }

    @GetMapping("/new")
    public String showNewReservationPage(Model model) {
        model.addAttribute("reservation", new ReservationRequest());

        return "receptionist/reservation-form";
    }
}
