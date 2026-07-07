package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Customer;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class CustomerResponseDTO {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String customerCode;
    private Integer loyaltyPoints;
    private LocalDateTime createdDate;
    // passwordHash intentionally excluded

    public static CustomerResponseDTO fromEntity(Customer c) {
        return CustomerResponseDTO.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .customerCode(c.getCustomerCode())
                .loyaltyPoints(c.getLoyaltyPoints())
                .createdDate(c.getCreatedDate())
                .build();
    }
}