package com.rroms.restaurantmanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = { ResourceNotFoundException.class })
    public String handleException(Exception exception, HttpServletRequest request, RedirectAttributes redirectAttributes) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ?  referer : "/customer/home");
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleException(RuntimeException exception, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ?  referer : "/customer/home");
    }
}
