package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Delivery;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class DeliveryResponseDTO {
    private Integer id;
    private String trackingCode;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private Integer orderId;
    private String orderCode;
    private Integer driverId;
    private String driverName;

    public static DeliveryResponseDTO fromEntity(Delivery d) {
        return DeliveryResponseDTO.builder()
                .id(d.getId())
                .trackingCode(d.getTrackingCode())
                .status(d.getStatus())
                .assignedAt(d.getAssignedAt())
                .pickedUpAt(d.getPickedUpAt())
                .deliveredAt(d.getDeliveredAt())
                .orderId(d.getOrder() != null ? d.getOrder().getId() : null)
                .orderCode(d.getOrder() != null ? d.getOrder().getOrderCode() : null)
                .driverId(d.getDriver() != null ? d.getDriver().getId() : null)
                .driverName(d.getDriver() != null
                        ? d.getDriver().getFirstName() + " " + d.getDriver().getLastName()
                        : null)
                .build();
    }
}