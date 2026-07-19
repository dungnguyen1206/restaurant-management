package com.rroms.restaurantmanagement.controller.customer;

import com.rroms.restaurantmanagement.dto.request.ProfileUpdateDTO;
import com.rroms.restaurantmanagement.dto.request.ReservationPaymentDTO;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.repository.UserRepository;
import com.rroms.restaurantmanagement.service.ReservationService;
import com.rroms.restaurantmanagement.service.RestaurantTableService;
import com.rroms.restaurantmanagement.service.UserService;
import com.rroms.restaurantmanagement.service.impl.ReservationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import org.springframework.data.domain.Page;
import com.rroms.restaurantmanagement.entity.Reservation;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final RestaurantTableService tableService;
    private final ReservationService reservationService;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/home")
    public String home() {
        return "redirect:/home";
    }

    @GetMapping("/reservations/available")
    public String availableTables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer capacity,
            Model model) {
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        if (selectedDate.isBefore(LocalDate.now())) {
            model.addAttribute("errorMessage", "Vui lòng chọn ngày hiện tại hoặc một ngày trong tương lai");
            selectedDate = LocalDate.now();
        }
        model.addAttribute("date", selectedDate);
        model.addAttribute("capacity", capacity);
        model.addAttribute("availableTables", tableService.findAvailableTables(selectedDate, capacity));
        return "search-available";
    }

    @PostMapping("/reservations/payment")
    public String paymentPage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<Long> tableIds,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = currentUser(authentication);
            List<RestaurantTable> tables = reservationService.getSelectedAvailableTables(date, tableIds);
            ReservationPaymentDTO form = new ReservationPaymentDTO();
            form.setDate(date);
            form.setTableIds(tables.stream().map(RestaurantTable::getTableId).toList());
            form.setFullName(fullName(user));
            form.setPhone(user.getPhone());
            preparePaymentModel(model, tables, form);
            return "customer/payment";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/customer/reservations/available?date=" + date;
        }
    }

    @PostMapping("/reservations/confirm-payment")
    public String confirmPayment(@Valid @ModelAttribute("paymentForm") ReservationPaymentDTO form,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                preparePaymentModel(model,
                        reservationService.getSelectedAvailableTables(form.getDate(), form.getTableIds()), form);
                return "customer/payment";
            } catch (RuntimeException exception) {
                redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                return "redirect:/customer/reservations/available";
            }
        }
        try {
            reservationService.createPaidReservation(currentUser(authentication), form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thanh toán thành công. Lịch đặt bàn đang chờ nhà hàng xác nhận.");
            return "redirect:/customer/reservations";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/customer/reservations/available?date=" + form.getDate();
        }
    }

    @GetMapping("/reservations")
    public String myReservations(@RequestParam(required = false) ReservationStatus status,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size,
                                 Authentication authentication, Model model) {
        Page<Reservation> result = reservationService.findMyReservations(
                currentUser(authentication), status, page, size);
        model.addAttribute("reservations", result.getContent());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", result.getSize());
        model.addAttribute("depositPerTable", ReservationServiceImpl.DEPOSIT_PER_TABLE);
        return "customer/my-reservations";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelPendingReservation(id, currentUser(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy lịch đặt bàn thành công.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/customer/reservations";
    }

    @PostMapping("/reservations/{id}/delete")
    public String deleteReservation(@PathVariable Long id, Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteCancelledReservation(id, currentUser(authentication));
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa vĩnh viễn lịch đặt bàn.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/customer/reservations?status=CANCELLED";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        if (!model.containsAttribute("profileForm")) {
            ProfileUpdateDTO form = new ProfileUpdateDTO();
            form.setFirstName(user.getFirstName());
            form.setMiddleName(user.getMiddleName());
            form.setLastName(user.getLastName());
            form.setPhone(user.getPhone());
            model.addAttribute("profileForm", form);
        }
        model.addAttribute("user", user);
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileUpdateDTO form,
                                BindingResult bindingResult, Authentication authentication,
                                Model model, RedirectAttributes redirectAttributes) {
        User user = currentUser(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "customer/profile";
        }
        userService.updateProfile(user.getUserId(), form);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật hồ sơ thành công.");
        return "redirect:/customer/profile";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản đang đăng nhập"));
    }

    private void preparePaymentModel(Model model, List<RestaurantTable> tables, ReservationPaymentDTO form) {
        model.addAttribute("selectedTables", tables);
        model.addAttribute("paymentForm", form);
        model.addAttribute("depositPerTable", ReservationServiceImpl.DEPOSIT_PER_TABLE);
        model.addAttribute("totalDeposit", ReservationServiceImpl.DEPOSIT_PER_TABLE
                .multiply(java.math.BigDecimal.valueOf(tables.size())));
    }

    private String fullName(User user) {
        return java.util.stream.Stream.of(user.getFirstName(), user.getMiddleName(), user.getLastName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
    }
}
