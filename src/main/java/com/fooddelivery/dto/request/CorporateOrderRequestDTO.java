package com.fooddelivery.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Valid;
import lombok.Data;
import java.util.List;

@Data
public class CorporateOrderRequestDTO {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String costCenter;

    @NotNull(message = "Restaurant ID is required")
    private Integer restaurantId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequestDTO> items;
}