package com.fooddelivery.dto.request;

import com.fooddelivery.entities.DeliveryDriver;
import com.fooddelivery.utils.HelperUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DriverRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone must be 8-15 digits")
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;

    private String vehicleType;
    private String vehiclePlate;

    public DeliveryDriver toEntity() {
        return DeliveryDriver.builder()
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .phone(this.phone)
                .passwordHash(this.password)
               .driverCode(HelperUtils.generateCode("DRV"))
                .vehicleType(this.vehicleType)
                .vehiclePlate(this.vehiclePlate)
                .currentLat(0.0)
                .currentLng(0.0)
                .isOnline(false)
                .isActive(true)
                .build();
    }
}