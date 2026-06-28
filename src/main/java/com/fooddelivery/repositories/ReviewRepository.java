package com.fooddelivery.repositories;

import com.fooddelivery.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId AND r.isActive = true")
    List<Review> findByRestaurantId(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId AND r.isActive = true")
    Page<Review> findByRestaurantIdPageable(@Param("restaurantId") Integer restaurantId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.driver.id = :driverId AND r.isActive = true")
    List<Review> findByDriverId(@Param("driverId") Integer driverId);

    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.isActive = true")
    Optional<Review> findActiveById(@Param("id") Integer id);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.restaurant.id = :restaurantId AND r.isActive = true")
    Double avgRatingByRestaurantId(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.driver.id = :driverId AND r.isActive = true")
    Double avgRatingByDriverId(@Param("driverId") Integer driverId);
}