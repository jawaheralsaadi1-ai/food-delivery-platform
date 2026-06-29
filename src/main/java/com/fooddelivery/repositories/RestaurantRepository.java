package com.fooddelivery.repositories;

import com.fooddelivery.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.cuisineType) = LOWER(:cuisineType) AND r.isActive = true")
    List<Restaurant> findByCuisineTypeIgnoreCase(@Param("cuisineType") String cuisineType);

    @Query("SELECT r FROM Restaurant r WHERE r.acceptingOrders = true AND r.isActive = true")
    List<Restaurant> findByAcceptingOrdersTrue();

    @Query("SELECT r FROM Restaurant r WHERE r.deliveryFee <= :fee AND r.isActive = true")
    List<Restaurant> findByDeliveryFeeLessThanEqual(@Param("fee") double fee);

    @Query("SELECT r FROM Restaurant r WHERE r.owner.id = :ownerId AND r.isActive = true")
    List<Restaurant> findByOwnerId(@Param("ownerId") Integer ownerId);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND r.isActive = true")
    List<Restaurant> findByNameKeyword(@Param("keyword") String keyword);

    @Query("SELECT r FROM Restaurant r WHERE r.id = :id AND r.isActive = true")
    Optional<Restaurant> findActiveById(@Param("id") Integer id);

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true")
    List<Restaurant> findAllActive();
    // add for getNearby() method
    @Query("SELECT r FROM Restaurant r WHERE r.latitude BETWEEN :minLat AND :maxLat AND r.longitude BETWEEN :minLng AND :maxLng AND r.isActive = true")
    List<Restaurant> findNearby(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                                @Param("minLng") double minLng, @Param("maxLng") double maxLng);
}