package com.fooddelivery.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerPatchDTO {

    private String firstName;
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone must be 8-15 digits")
    private String phone;
}