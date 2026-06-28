package com.fooddelivery.dto.response;

import com.fooddelivery.entities.MenuItem;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class MenuItemResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private Boolean isAvailable;
    private Boolean isVegetarian;
    private Integer calories;
    private Integer restaurantId;

    public static MenuItemResponseDTO fromEntity(MenuItem m) {
        return MenuItemResponseDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .isAvailable(m.getIsAvailable())
                .isVegetarian(m.getIsVegetarian())
                .calories(m.getCalories())
                .restaurantId(m.getRestaurant() != null ? m.getRestaurant().getId() : null)
                .build();
    }
}