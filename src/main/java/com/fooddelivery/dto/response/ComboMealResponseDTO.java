package com.fooddelivery.dto.response;

import com.fooddelivery.entities.ComboMeal;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder
public class ComboMealResponseDTO {
    private Integer id;
    private String comboName;
    private String description;
    private Double totalPrice;
    private Boolean isAvailable;
    private Integer restaurantId;
    private List<MenuItemResponseDTO> menuItems;

    public static ComboMealResponseDTO fromEntity(ComboMeal c) {
        return ComboMealResponseDTO.builder()
                .id(c.getId())
                .comboName(c.getComboName())
                .description(c.getDescription())
                .totalPrice(c.getTotalPrice())
                .isAvailable(c.getIsAvailable())
                .restaurantId(c.getRestaurant() != null ? c.getRestaurant().getId() : null)
                .menuItems(c.getMenuItems() != null
                        ? c.getMenuItems().stream()
                          .filter(m -> m.getIsActive())
                          .map(MenuItemResponseDTO::fromEntity)
                          .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}