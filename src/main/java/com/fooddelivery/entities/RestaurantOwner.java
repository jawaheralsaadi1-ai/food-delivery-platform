package com.fooddelivery.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "restaurant_owners")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RestaurantOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String passwordHash;

    private String businessLicenseCode;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate;

    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Restaurant> restaurants;
}