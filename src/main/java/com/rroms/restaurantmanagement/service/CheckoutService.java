package com.rroms.restaurantmanagement.service;

import com.rroms.restaurantmanagement.dto.response.CheckoutViewDTO;
import com.rroms.restaurantmanagement.entity.constant.PaymentMethod;

public interface CheckoutService {
    CheckoutViewDTO getCheckoutView(Long orderId);

    void confirmPaid(Long orderId, PaymentMethod paymentMethod);
}
