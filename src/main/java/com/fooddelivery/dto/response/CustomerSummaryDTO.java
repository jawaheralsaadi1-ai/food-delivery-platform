package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Customer;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CustomerSummaryDTO {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;

    public static CustomerSummaryDTO fromEntity(Customer c) {
        return CustomerSummaryDTO.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .build();
    }
}