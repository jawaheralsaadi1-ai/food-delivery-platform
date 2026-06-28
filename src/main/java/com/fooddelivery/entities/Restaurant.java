package com.fooddelivery.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String cuisineType;

    private String openingTime;

    private String closingTime;

    @Builder.Default
    private Double minOrderAmount = 0.0;

    @Builder.Default
    private Double deliveryFee = 0.0;

    @Builder.Default
    private Boolean acceptingOrders = true;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate;

    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private RestaurantOwner owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComboMeal> comboMeals;
}