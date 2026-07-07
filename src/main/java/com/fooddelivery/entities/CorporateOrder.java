package com.fooddelivery.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "corporate_orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CorporateOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String corporateCode;

    @Column(nullable = false)
    private String companyName;

    private String costCenter;

    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    @Builder.Default
    private String status = "PENDING";

    @Builder.Default
    private Double totalAmount = 0.0;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate;

    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "corporateOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CorporateOrderItem> corporateOrderItems;
}