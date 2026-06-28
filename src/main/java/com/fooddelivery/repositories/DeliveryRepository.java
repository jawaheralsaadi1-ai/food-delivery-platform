package com.fooddelivery.repositories;

import com.fooddelivery.entities.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.status = :status AND d.isActive = true")
    List<Delivery> findByDeliveryDriverIdAndStatus(@Param("driverId") Integer driverId,
                                                   @Param("status") String status);

    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.isActive = true")
    List<Delivery> findByDriverId(@Param("driverId") Integer driverId);

    @Query("SELECT d FROM Delivery d WHERE d.status = :status AND d.isActive = true")
    List<Delivery> findByStatus(@Param("status") String status);

    @Query("SELECT d FROM Delivery d WHERE d.order.id = :orderId AND d.isActive = true")
    Optional<Delivery> findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT d FROM Delivery d WHERE d.id = :id AND d.isActive = true")
    Optional<Delivery> findActiveById(@Param("id") Integer id);

    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.status NOT IN ('DELIVERED', 'CANCELLED') AND d.isActive = true")
    Optional<Delivery> findActiveDeliveryByDriverId(@Param("driverId") Integer driverId);

    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driver.id = :driverId AND d.status = 'DELIVERED' AND d.isActive = true")
    Long countCompletedByDriverId(@Param("driverId") Integer driverId);

    @Query(name = "SELECT COALESCE(AVG(TIMESTAMPDIFF(MINUTE, d.assigned_at, d.delivered_at)), 0) FROM deliveries d WHERE d.driver_id = :driverId AND d.status = 'DELIVERED' AND d.is_active = true",
            nativeQuery = true)
    Double avgDeliveryTimeByDriverId(@Param("driverId") Integer driverId);

    @Query("SELECT COALESCE(SUM(o.deliveryFee), 0) FROM Delivery d JOIN d.order o WHERE d.driver.id = :driverId AND d.status = 'DELIVERED' AND d.assignedAt BETWEEN :start AND :end AND d.isActive = true")
    Double sumEarningsByDriverAndDateRange(@Param("driverId") Integer driverId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);
}