package com.fooddelivery.repositories;

import com.fooddelivery.entities.DeliveryDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryDriverRepository extends JpaRepository<DeliveryDriver, Integer> {

    @Query("SELECT d FROM DeliveryDriver d WHERE d.isOnline = true AND d.isActive = true")
    List<DeliveryDriver> findAllOnline();

    @Query("SELECT d FROM DeliveryDriver d WHERE d.isActive = true")
    List<DeliveryDriver> findAllActive();

    @Query("SELECT d FROM DeliveryDriver d WHERE d.id = :id AND d.isActive = true")
    Optional<DeliveryDriver> findActiveById(@Param("id") Integer id);

    @Query("SELECT d FROM DeliveryDriver d WHERE d.email = :email AND d.isActive = true")
    Optional<DeliveryDriver> findByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM delivery_drivers WHERE is_online = true AND is_active = true LIMIT 1",
            nativeQuery = true)
    Optional<DeliveryDriver> findFirstOnlineDriver();

    @Query(value = "SELECT dd.* FROM delivery_drivers dd " +
            "JOIN deliveries d ON d.driver_id = dd.id " +
            "WHERE d.status = 'DELIVERED' AND dd.is_active = true " +
            "GROUP BY dd.id ORDER BY COUNT(d.id) DESC LIMIT :limit",
            nativeQuery = true)
    List<DeliveryDriver> findTopDriversByCompletedDeliveries(@Param("limit") int limit);
}