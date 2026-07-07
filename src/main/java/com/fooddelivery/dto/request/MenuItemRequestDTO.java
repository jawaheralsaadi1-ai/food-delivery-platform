package com.fooddelivery.dto.request;

import com.fooddelivery.entities.MenuItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MenuItemRequestDTO {

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", message = "Price must be zero or positive")
    private Double price;

    private Boolean isAvailable = true;
    private Boolean isVegetarian = false;

    @PositiveOrZero(message = "Calories must be zero or positive")
    private Integer calories;

    public MenuItem toEntity() {
        return MenuItem.builder()
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .isAvailable(this.isAvailable != null ? this.isAvailable : true)
                .isVegetarian(this.isVegetarian != null ? this.isVegetarian : false)
                .calories(this.calories)
                .isActive(true)
                .build();
    }
}