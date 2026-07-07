package com.fooddelivery.dto.response;

import com.fooddelivery.entities.DeliveryDriver;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class DriverResponseDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String driverCode;
    private String vehicleType;
    private String vehiclePlate;
    private Double currentLat;
    private Double currentLng;
    private Boolean isOnline;
    private LocalDateTime createdDate;

    public static DriverResponseDTO fromEntity(DeliveryDriver d) {
        return DriverResponseDTO.builder()
                .id(d.getId())
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .email(d.getEmail())
                .phone(d.getPhone())
                .driverCode(d.getDriverCode())
                .vehicleType(d.getVehicleType())
                .vehiclePlate(d.getVehiclePlate())
                .currentLat(d.getCurrentLat())
                .currentLng(d.getCurrentLng())
                .isOnline(d.getIsOnline())
                .createdDate(d.getCreatedDate())
                .build();
    }
}