package com.fooddelivery.dto.request;

import com.fooddelivery.entities.ComboMeal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ComboMealRequestDTO {

    @NotBlank(message = "Combo name is required")
    private String comboName;

    private String description;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", message = "Total price must be zero or positive")
    private Double totalPrice;

    @NotEmpty(message = "Combo must contain at least one item")
    private List<Integer> menuItemIds;

    public ComboMeal toEntity() {
        return ComboMeal.builder()
                .comboName(this.comboName)
                .description(this.description)
                .totalPrice(this.totalPrice)
                .isAvailable(true)
                .isActive(true)
                .build();
    }
}