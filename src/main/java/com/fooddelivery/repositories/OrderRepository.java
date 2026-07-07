package com.fooddelivery.repositories;

import com.fooddelivery.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.isActive = true")
    List<Order> findByCustomerId(@Param("customerId") Integer customerId);

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status = :status AND o.isActive = true")
    List<Order> findByRestaurantIdAndStatus(@Param("restaurantId") Integer restaurantId,
                                            @Param("status") String status);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :start AND :end AND o.isActive = true")
    List<Order> findByOrderDateBetween(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.isActive = true")
    Optional<Order> findActiveById(@Param("id") Integer id);

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.isActive = true")
    List<Order> findByRestaurantId(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status = 'DELIVERED' AND o.isActive = true")
    Long countCompletedOrdersByRestaurantId(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status = 'DELIVERED' AND DATE(o.orderDate) = DATE(:date) AND o.isActive = true")
    Double sumDeliveredRevenueByRestaurantAndDate(@Param("restaurantId") Integer restaurantId,
                                                  @Param("date") LocalDateTime date);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND (:status IS NULL OR o.status = :status) AND (CAST(:start AS timestamp) IS NULL OR o.orderDate >= :start) AND (CAST(:end AS timestamp) IS NULL OR o.orderDate <= :end) AND o.isActive = true")
    Page<Order> findByCustomerFiltered(@Param("customerId") Integer customerId,
                                       @Param("status") String status,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED' AND o.orderDate BETWEEN :start AND :end AND o.isActive = true")
    Long countCancelledBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :start AND :end AND o.isActive = true")
    Long countTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT HOUR(order_date) as hour, COUNT(*) as cnt FROM orders WHERE is_active = true GROUP BY HOUR(order_date) ORDER BY hour",
            nativeQuery = true)
    List<Object[]> findOrderVolumeByHour();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status = 'DELIVERED' AND o.orderDate BETWEEN :start AND :end AND o.isActive = true")
    Double sumRevenueByRestaurantAndDateRange(@Param("restaurantId") Integer restaurantId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED' AND DATE(o.orderDate) = DATE(:date) AND o.isActive = true")
    Long countDeliveredOnDate(@Param("date") LocalDateTime date);

    @Query("SELECT COALESCE(SUM(o.deliveryFee), 0) FROM Order o WHERE DATE(o.orderDate) = DATE(:date) AND o.isActive = true")
    Double sumDeliveryFeesOnDate(@Param("date") LocalDateTime date);
}