package com.fooddelivery.repositories;

import com.fooddelivery.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.isActive = true")
    Optional<Customer> findByEmail(@Param("email") String email);

    @Query("SELECT c FROM Customer c WHERE c.loyaltyPoints >= :points AND c.isActive = true")
    List<Customer> findByLoyaltyPointsGreaterThanEqual(@Param("points") int points);

    @Query("SELECT c FROM Customer c WHERE c.createdDate BETWEEN :start AND :end AND c.isActive = true")
    List<Customer> findByCreatedDateBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT c FROM Customer c WHERE c.isActive = true")
    List<Customer> findAllActive();

    @Query("SELECT c FROM Customer c WHERE (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND c.isActive = true")
    Page<Customer> searchByName(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT * FROM customers WHERE is_active = true ORDER BY loyalty_points DESC LIMIT :limit",
            nativeQuery = true)
    List<Customer> findTopByLoyaltyPoints(@Param("limit") int limit);
}