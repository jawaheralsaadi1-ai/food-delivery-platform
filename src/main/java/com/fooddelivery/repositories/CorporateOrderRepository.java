package com.fooddelivery.repositories;

import com.fooddelivery.entities.CorporateOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CorporateOrderRepository extends JpaRepository<CorporateOrder, Integer> {

    @Query("SELECT co FROM CorporateOrder co WHERE co.isActive = true")
    List<CorporateOrder> findAllActive();

    @Query("SELECT co FROM CorporateOrder co WHERE co.id = :id AND co.isActive = true")
    Optional<CorporateOrder> findActiveById(@Param("id") Integer id);

    @Query("SELECT co FROM CorporateOrder co WHERE co.restaurant.id = :restaurantId AND co.isActive = true")
    List<CorporateOrder> findByRestaurantId(@Param("restaurantId") Integer restaurantId);
}