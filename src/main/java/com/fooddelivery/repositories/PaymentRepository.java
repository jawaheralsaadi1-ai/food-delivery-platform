package com.fooddelivery.repositories;

import com.fooddelivery.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.isActive = true")
    Optional<Payment> findByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.isActive = true")
    Optional<Payment> findActiveById(@Param("id") Integer id);

    @Query("SELECT p FROM Payment p WHERE (:method IS NULL OR p.paymentMethod = :method) AND (:status IS NULL OR p.status = :status) AND (CAST(:start AS timestamp) IS NULL OR p.processedAt >= :start) AND (CAST(:end AS timestamp) IS NULL OR p.processedAt <= :end) AND p.isActive = true")
    Page<Payment> findFiltered(@Param("method") String method,
                               @Param("status") String status,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               Pageable pageable);

    @Query("SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.isActive = true GROUP BY p.paymentMethod")
    List<Object[]> sumAmountGroupedByMethod();
}