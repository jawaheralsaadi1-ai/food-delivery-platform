package com.fooddelivery.dto.request;

import com.fooddelivery.entities.Restaurant;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class RestaurantRequestDTO {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    @NotBlank(message = "Cuisine type is required")
    private String cuisineType;

    private String openingTime;
    private String closingTime;

    @PositiveOrZero(message = "Minimum order amount must be zero or positive")
    private Double minOrderAmount = 0.0;

    @PositiveOrZero(message = "Delivery fee must be zero or positive")
    private Double deliveryFee = 0.0;

    public Restaurant toEntity() {
        return Restaurant.builder()
                .name(this.name)
                .description(this.description)
                .cuisineType(this.cuisineType)
                .openingTime(this.openingTime)
                .closingTime(this.closingTime)
                .minOrderAmount(this.minOrderAmount != null ? this.minOrderAmount : 0.0)
                .deliveryFee(this.deliveryFee != null ? this.deliveryFee : 0.0)
                .acceptingOrders(true)
                .isActive(true)
                .build();
    }
}