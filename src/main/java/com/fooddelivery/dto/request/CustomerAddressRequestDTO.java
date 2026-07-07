package com.fooddelivery.dto.request;

import com.fooddelivery.entities.CustomerAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerAddressRequestDTO {

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    private String building;

    private Boolean isDefault = false;

    public CustomerAddress toEntity() {
        return CustomerAddress.builder()
                .street(this.street)
                .city(this.city)
                .building(this.building)
                .isDefault(this.isDefault != null ? this.isDefault : false)
                .isActive(true)
                .build();
    }
}