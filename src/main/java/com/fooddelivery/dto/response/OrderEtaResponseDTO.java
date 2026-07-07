package com.fooddelivery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEtaResponseDTO {
    private Integer orderId;
    private String orderStatus;
    private Boolean driverAssigned;
    private Integer driverId;
    private Double distanceKm;
    private Integer etaMinutes;
    private LocalDateTime estimatedDeliveryTime;
    private String message;
}