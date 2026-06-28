package com.fooddelivery.dto.response;

import com.fooddelivery.entities.CustomerAddress;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AddressResponseDTO {
    private Integer id;
    private String street;
    private String city;
    private String building;
    private Boolean isDefault;

    public static AddressResponseDTO fromEntity(CustomerAddress a) {
        return AddressResponseDTO.builder()
                .id(a.getId())
                .street(a.getStreet())
                .city(a.getCity())
                .building(a.getBuilding())
                .isDefault(a.getIsDefault())
                .build();
    }
}