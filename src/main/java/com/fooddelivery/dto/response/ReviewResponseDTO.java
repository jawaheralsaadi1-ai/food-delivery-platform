package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Review;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ReviewResponseDTO {
    private Integer id;
    private String targetType;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private CustomerSummaryDTO customer;
    private Integer restaurantId;
    private Integer driverId;

    public static ReviewResponseDTO fromEntity(Review r) {
        return ReviewResponseDTO.builder()
                .id(r.getId())
                .targetType(r.getTargetType())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .customer(r.getCustomer() != null
                        ? CustomerSummaryDTO.fromEntity(r.getCustomer()) : null)
                .restaurantId(r.getRestaurant() != null ? r.getRestaurant().getId() : null)
                .driverId(r.getDriver() != null ? r.getDriver().getId() : null)
                .build();
    }
}