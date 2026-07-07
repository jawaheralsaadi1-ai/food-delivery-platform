package com.fooddelivery.dto.response;

import com.fooddelivery.entities.RestaurantOwner;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class RestaurantOwnerResponseDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String businessLicenseCode;
    private LocalDateTime createdDate;
    // passwordHash intentionally excluded

    public static RestaurantOwnerResponseDTO fromEntity(RestaurantOwner o) {
        return RestaurantOwnerResponseDTO.builder()
                .id(o.getId())
                .firstName(o.getFirstName())
                .lastName(o.getLastName())
                .email(o.getEmail())
                .phone(o.getPhone())
                .businessLicenseCode(o.getBusinessLicenseCode())
                .createdDate(o.getCreatedDate())
                .build();
    }
}