package com.fooddelivery.dto.response;

import com.fooddelivery.entities.OrderItem;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class OrderItemResponseDTO {
    private Integer id;
    private Integer menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String specialInstructions;

    public static OrderItemResponseDTO fromEntity(OrderItem oi) {
        return OrderItemResponseDTO.builder()
                .id(oi.getId())
                .menuItemId(oi.getMenuItem() != null ? oi.getMenuItem().getId() : null)
                .menuItemName(oi.getMenuItem() != null ? oi.getMenuItem().getName() : null)
                .quantity(oi.getQuantity())
                .unitPrice(oi.getUnitPrice())
                .totalPrice(oi.getTotalPrice())
                .specialInstructions(oi.getSpecialInstructions())
                .build();
    }
}