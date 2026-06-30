package com.smartcafe.website.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReservationRequest {
    @NotNull private Long tableId;
    @Min(1) @Max(20) private int partySize = 1;
    @NotBlank private String reservationDate;
    @NotBlank private String reservationTime;
    private String notes;
}
