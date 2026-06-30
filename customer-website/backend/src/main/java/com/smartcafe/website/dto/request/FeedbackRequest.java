package com.smartcafe.website.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FeedbackRequest {
    private Long menuItemId;
    private Long orderId;

    @Min(1) @Max(5)
    private int rating = 5;

    @Size(max = 1000)
    private String review;
}
