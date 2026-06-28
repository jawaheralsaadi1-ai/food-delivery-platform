package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Restaurant;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class RestaurantResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private String cuisineType;
    private String openingTime;
    private String closingTime;
    private Double minOrderAmount;
    private Double deliveryFee;
    private Boolean acceptingOrders;
    private Integer ownerId;
    private String ownerName;
    private LocalDateTime createdDate;

    public static RestaurantResponseDTO fromEntity(Restaurant r) {
        return RestaurantResponseDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .cuisineType(r.getCuisineType())
                .openingTime(r.getOpeningTime())
                .closingTime(r.getClosingTime())
                .minOrderAmount(r.getMinOrderAmount())
                .deliveryFee(r.getDeliveryFee())
                .acceptingOrders(r.getAcceptingOrders())
                .ownerId(r.getOwner() != null ? r.getOwner().getId() : null)
                .ownerName(r.getOwner() != null
                        ? r.getOwner().getFirstName() + " " + r.getOwner().getLastName()
                        : null)
                .createdDate(r.getCreatedDate())
                .build();
    }
}
