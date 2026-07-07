package com.fooddelivery.repositories;

import com.fooddelivery.entities.ComboMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComboMealRepository extends JpaRepository<ComboMeal, Integer> {

    @Query("SELECT c FROM ComboMeal c WHERE c.restaurant.id = :restaurantId AND c.isActive = true")
    List<ComboMeal> findByRestaurantId(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT c FROM ComboMeal c WHERE c.restaurant.id = :restaurantId AND c.isAvailable = true AND c.isActive = true")
    List<ComboMeal> findByRestaurantIdAndIsAvailableTrue(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT c FROM ComboMeal c JOIN c.menuItems m WHERE m.id = :menuItemId AND c.isActive = true")
    List<ComboMeal> findByMenuItemId(@Param("menuItemId") Integer menuItemId);

    @Query("SELECT c FROM ComboMeal c WHERE c.id = :id AND c.isActive = true")
    Optional<ComboMeal> findActiveById(@Param("id") Integer id);
}