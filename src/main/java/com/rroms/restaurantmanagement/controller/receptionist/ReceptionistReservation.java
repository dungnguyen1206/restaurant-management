package com.rroms.restaurantmanagement.controller.receptionist;

import com.rroms.restaurantmanagement.dto.request.ConfirmReservationRequest;
import com.rroms.restaurantmanagement.dto.request.ReservationRequest;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import com.rroms.restaurantmanagement.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import lombok.*;
import com.rroms.restaurantmanagement.dto.request.WalkInRequest;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.repository.TableRepository;
@Controller
@RequestMapping("/receptionist/reservations")
@RequiredArgsConstructor
public class ReceptionistReservation {
    private final TableRepository tableRepository;
    private final ReservationService reservationService;
    @GetMapping
    public String listReservation(Model model,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String status) {
        model.addAttribute("reservations", reservationService.getReservationList(keyword,status));

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "Reservations");
        model.addAttribute("currentPage", "reservations");
        return "receptionist/reservations";
    }

    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable Long id){
        reservationService.checkIn(id);
        return "redirect:/receptionist/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String checkOut(@PathVariable Long id){
        reservationService.cancel(id);
        return "redirect:/receptionist/reservations";
    }

    @GetMapping("/new")
    public String showNewReservationPage(Model model) {
        model.addAttribute("reservation", new ReservationRequest());

        return "receptionist/reservation-form";
    }

    @GetMapping("/walk-in")
    public String showWalkInPage(@RequestParam Long tableId, Model model) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table không tồn tại"));

        WalkInRequest request = new WalkInRequest();
        request.setTableId(tableId);
        request.setNumberOfGuests(table.getCapacity());

        model.addAttribute("walkIn", request);
        model.addAttribute("table", table);
        model.addAttribute("pageTitle", "Walk-in");
        model.addAttribute("currentPage", "tables");

        return "receptionist/walk-in-form";
    }

    @PostMapping("/walk-in")
    public String createWalkIn(@ModelAttribute("walkIn") WalkInRequest request) {
        reservationService.createWalkIn(request);
        return "redirect:/receptionist/tables";
    }

    @GetMapping("/{id}/confirm")
    public String showConfirmPage(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationById(id);

        List<RestaurantTable> availableTables =
                tableRepository.findByStatusAndCapacityGreaterThanEqualOrderByTableNumberAsc(
                        TableStatus.AVAILABLE,
                        reservation.getNumberOfGuests()
                );

        ConfirmReservationRequest request = new ConfirmReservationRequest();

        model.addAttribute("reservation", reservation);
        model.addAttribute("confirmRequest", request);
        model.addAttribute("tables", availableTables);
        model.addAttribute("pageTitle", "Confirm Reservation");
        model.addAttribute("currentPage", "reservations");

        return "receptionist/reservation-confirm";
    }

    @PostMapping("/{id}/confirm")
    public String confirmReservation(
            @PathVariable Long id,
            @ModelAttribute("confirmRequest") ConfirmReservationRequest request
    ) {
        reservationService.confirm(id, request.getTableId());
        return "redirect:/receptionist/reservations";
    }
}
