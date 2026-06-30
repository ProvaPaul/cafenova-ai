package com.smartcafe.website.dto.request;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String orderType = "TAKEAWAY";
    private String notes;
    private String couponCode;
    private String deliveryAddress;
}
