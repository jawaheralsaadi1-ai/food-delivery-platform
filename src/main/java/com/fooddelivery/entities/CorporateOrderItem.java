package com.fooddelivery.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "corporate_order_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CorporateOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Double totalPrice;

    private String specialInstructions;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime updatedDate;

    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_order_id", nullable = false)
    private CorporateOrder corporateOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;
}