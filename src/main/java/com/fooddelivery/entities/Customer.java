package com.fooddelivery.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

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

    private String passwordHash;

    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Column(unique = true)
    private String customerCode;

    //  Standard fields entity
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @Builder.Default
    private Boolean isActive = true;

    //  Relationships
   // @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
   // private List<CustomerAddress> addresses;

   // @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
   // private List<Order> orders;

   // @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    //private List<Review> reviews;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
