package com.fooddelivery.dto.response;

import com.fooddelivery.entities.CorporateOrder;
import com.fooddelivery.entities.CorporateOrderItem;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder
public class CorporateOrderResponseDTO {
    private Integer id;
    private String corporateCode;
    private String companyName;
    private String costCenter;
    private LocalDateTime orderDate;
    private String status;
    private Double totalAmount;
    private RestaurantSummaryDTO restaurant;
    private List<CorporateOrderItemResponseDTO> items;

    @Data @Builder
    public static class CorporateOrderItemResponseDTO {
        private Integer id;
        private Integer menuItemId;
        private String menuItemName;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
        private String specialInstructions;

        public static CorporateOrderItemResponseDTO fromEntity(CorporateOrderItem ci) {
            return CorporateOrderItemResponseDTO.builder()
                    .id(ci.getId())
                    .menuItemId(ci.getMenuItem() != null ? ci.getMenuItem().getId() : null)
                    .menuItemName(ci.getMenuItem() != null ? ci.getMenuItem().getName() : null)
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice())
                    .totalPrice(ci.getTotalPrice())
                    .specialInstructions(ci.getSpecialInstructions())
                    .build();
        }
    }

    public static CorporateOrderResponseDTO fromEntity(CorporateOrder co) {
        return CorporateOrderResponseDTO.builder()
                .id(co.getId())
                .corporateCode(co.getCorporateCode())
                .companyName(co.getCompanyName())
                .costCenter(co.getCostCenter())
                .orderDate(co.getOrderDate())
                .status(co.getStatus())
                .totalAmount(co.getTotalAmount())
                .restaurant(co.getRestaurant() != null
                        ? RestaurantSummaryDTO.fromEntity(co.getRestaurant()) : null)
                .items(co.getCorporateOrderItems() != null
                        ? co.getCorporateOrderItems().stream()
                          .filter(i -> i.getIsActive())
                          .map(CorporateOrderItemResponseDTO::fromEntity)
                          .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}