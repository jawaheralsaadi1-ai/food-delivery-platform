package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Restaurant;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class RestaurantSummaryDTO {
    private Integer id;
    private String name;
    private String cuisineType;
    private Double deliveryFee;

    public static RestaurantSummaryDTO fromEntity(Restaurant r) {
        return RestaurantSummaryDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .cuisineType(r.getCuisineType())
                .deliveryFee(r.getDeliveryFee())
                .build();
    }
}