package com.rroms.restaurantmanagement.controller.receptionist;

import com.rroms.restaurantmanagement.dto.request.AssignWaiterRequest;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.service.TableService;
import org.springframework.stereotype.Controller;
import lombok.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.repository.ReservationRepository;
import com.rroms.restaurantmanagement.repository.projection.OrderListProjection;
import com.rroms.restaurantmanagement.repository.projection.ReservationProjection;
import com.rroms.restaurantmanagement.repository.projection.TableView;
import com.rroms.restaurantmanagement.service.OrderService;

import java.util.ArrayList;
import java.util.List;
@Controller
@RequiredArgsConstructor
@RequestMapping("/receptionist")
public class ReceptionistController {
    private final TableService tableService;
    private final OrderService orderService;
    private final ReservationRepository reservationRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ReservationProjection> reservations = new ArrayList<>();
        reservations.addAll(reservationRepository.searchReservations(null, null, ReservationStatus.PENDING));
        reservations.addAll(reservationRepository.searchReservations(null, null, ReservationStatus.CONFIRMED));
        reservations.addAll(reservationRepository.searchReservations(null, null, ReservationStatus.CHECKED_IN));

        List<TableView> tables = tableService.filterTables(null, null, null);

        List<OrderListProjection> orders = new ArrayList<>();
        orders.addAll(orderService.getReceptionistOrderList(null, "PENDING", 0, 5).getContent());
        orders.addAll(orderService.getReceptionistOrderList(null, "PREPARING", 0, 5).getContent());
        orders.addAll(orderService.getReceptionistOrderList(null, "SERVED", 0, 5).getContent());

        model.addAttribute("reservations", reservations.stream().limit(8).toList());
        model.addAttribute("tables", tables.stream().limit(12).toList());
        model.addAttribute("orders", orders.stream().limit(8).toList());

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("currentPage", "dashboard");

        return "receptionist/dashboard";
    }

    @GetMapping("/tables")
    public String listTable(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String capacity,
            Model model
    ){
        model.addAttribute("tables",  tableService.filterTables(keyword, area, capacity));

        model.addAttribute("keyword",keyword);
        model.addAttribute("area",area);
        model.addAttribute("capacity",capacity);
        model.addAttribute("pageTitle", "Tables");
        model.addAttribute("currentPage", "tables");
        return "receptionist/roomlist";
    }

    @GetMapping("/tables/{id}/assign-waiter")
    public String showAssignWaiterPage(@PathVariable Long id, Model model) {
        RestaurantTable table = tableService.getTableById(id);

        AssignWaiterRequest request = new AssignWaiterRequest();
        if (table.getAssignedWaiter() != null) {
            request.setWaiterId(table.getAssignedWaiter().getUserId());
        }

        model.addAttribute("table", table);
        model.addAttribute("waiters", tableService.getWaiters());
        model.addAttribute("assignWaiter", request);
        model.addAttribute("pageTitle", "Assign Waiter");
        model.addAttribute("currentPage", "tables");

        return "receptionist/assign-waiter";
    }

    @PostMapping("/tables/{id}/assign-waiter")
    public String assignWaiter(
            @PathVariable Long id,
            @ModelAttribute("assignWaiter") AssignWaiterRequest request
    ) {
        tableService.assignWaiter(id, request.getWaiterId());
        return "redirect:/receptionist/tables";
    }


}
