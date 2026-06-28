package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Order;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder
public class OrderResponseDTO {
    private Integer id;
    private String orderCode;
    private LocalDateTime orderDate;
    private String status;
    private Double subtotal;
    private Double deliveryFee;
    private Double discountAmount;
    private Double totalAmount;
    private String deliveryNotes;
    private CustomerSummaryDTO customer;
   // private RestaurantSummaryDTO restaurant;
    private List<OrderItemResponseDTO> items;

    public static OrderResponseDTO fromEntity(Order o) {
        return OrderResponseDTO.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .orderDate(o.getOrderDate())
                .status(o.getStatus())
                .subtotal(o.getSubtotal())
                .deliveryFee(o.getDeliveryFee())
                .discountAmount(o.getDiscountAmount())
                .totalAmount(o.getTotalAmount())
                .deliveryNotes(o.getDeliveryNotes())
                .customer(o.getCustomer() != null
                        ? CustomerSummaryDTO.fromEntity(o.getCustomer()) : null)
              //  .restaurant(o.getRestaurant() != null
                   //     ? RestaurantSummaryDTO.fromEntity(o.getRestaurant()) : null)
                .items(o.getOrderItems() != null
                        ? o.getOrderItems().stream()
                          .filter(i -> i.getIsActive())
                          .map(OrderItemResponseDTO::fromEntity)
                          .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
