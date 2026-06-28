package com.fooddelivery.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "menu_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    @Builder.Default
    private Boolean isAvailable = true;

    @Builder.Default
    private Boolean isVegetarian = false;

    private Integer calories;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate;

    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @ManyToMany(mappedBy = "menuItems")
    private List<ComboMeal> comboMeals;
}