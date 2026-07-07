package com.fooddelivery.repositories;

import com.fooddelivery.entities.CorporateOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorporateOrderItemRepository extends JpaRepository<CorporateOrderItem, Integer> {

    @Query("SELECT i FROM CorporateOrderItem i WHERE i.corporateOrder.id = :corporateOrderId AND i.isActive = true")
    List<CorporateOrderItem> findByCorporateOrderId(@Param("corporateOrderId") Integer corporateOrderId);
}