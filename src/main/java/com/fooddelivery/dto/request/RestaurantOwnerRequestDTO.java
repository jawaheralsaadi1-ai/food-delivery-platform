package com.fooddelivery.dto.request;

import com.fooddelivery.entities.RestaurantOwner;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RestaurantOwnerRequestDTO {

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

    private String businessLicenseCode;

    public RestaurantOwner toEntity() {
        return RestaurantOwner.builder()
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .phone(this.phone)
                .passwordHash(this.password)
                .businessLicenseCode(this.businessLicenseCode)
                .isActive(true)
                .build();
    }
}