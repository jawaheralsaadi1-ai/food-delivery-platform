package com.fooddelivery.repositories;

import com.fooddelivery.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.isActive = true")
    List<MenuItem> findByRestaurantId(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.isAvailable = true AND m.isActive = true")
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(@Param("restaurantId") Integer restaurantId);

    @Query("SELECT m FROM MenuItem m WHERE m.isVegetarian = true AND m.isActive = true")
    List<MenuItem> findByIsVegetarianTrue();

    @Query("SELECT m FROM MenuItem m WHERE m.price BETWEEN :min AND :max AND m.isActive = true")
    List<MenuItem> findByPriceBetween(@Param("min") double min, @Param("max") double max);

    @Query("SELECT m FROM MenuItem m WHERE m.id = :id AND m.isActive = true")
    Optional<MenuItem> findActiveById(@Param("id") Integer id);

    @Query("SELECT m FROM MenuItem m WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND m.isActive = true")
    List<MenuItem> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT m FROM MenuItem m WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND m.calories BETWEEN :minCal AND :maxCal AND m.isActive = true")
    List<MenuItem> searchByKeywordAndCalories(@Param("keyword") String keyword,
                                              @Param("minCal") int minCal,
                                              @Param("maxCal") int maxCal);

    @Query(value = "SELECT mi.* FROM menu_items mi " +
            "JOIN order_items oi ON oi.menu_item_id = mi.id " +
            "WHERE mi.restaurant_id = :restaurantId AND mi.is_active = true " +
            "GROUP BY mi.id ORDER BY SUM(oi.quantity) DESC LIMIT 5",
            nativeQuery = true)
    List<MenuItem> findTopSellersByRestaurantId(@Param("restaurantId") Integer restaurantId);
}