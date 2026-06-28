package com.fooddelivery.repositories;

import com.fooddelivery.entities.RestaurantOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RestaurantOwnerRepository extends JpaRepository<RestaurantOwner, Integer> {

    @Query("SELECT o FROM RestaurantOwner o WHERE o.email = :email AND o.isActive = true")
    Optional<RestaurantOwner> findByEmail(@Param("email") String email);

    @Query("SELECT o FROM RestaurantOwner o WHERE o.id = :id AND o.isActive = true")
    Optional<RestaurantOwner> findActiveById(@Param("id") Integer id);
}