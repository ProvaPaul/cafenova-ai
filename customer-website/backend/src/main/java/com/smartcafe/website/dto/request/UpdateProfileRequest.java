package com.smartcafe.website.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank @Size(min = 2, max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Email @Size(max = 100)
    private String email;

    private String address;
}
