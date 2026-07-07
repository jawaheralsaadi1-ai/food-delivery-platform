package com.fooddelivery.services;

import com.fooddelivery.dto.response.PaymentResponseDTO;
import com.fooddelivery.entities.Order;
import com.fooddelivery.entities.Payment;
import com.fooddelivery.exceptions.DuplicateResourceException;
import com.fooddelivery.exceptions.InvalidOrderStateException;
import com.fooddelivery.exceptions.ResourceNotFoundException;
import com.fooddelivery.repositories.OrderRepository;
import com.fooddelivery.repositories.PaymentRepository;
import com.fooddelivery.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;

    @Transactional
    public PaymentResponseDTO processPayment(Integer orderId, String method) {
        Order order = findActiveOrder(orderId);

        if ("CANCELLED".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Cannot create a payment for a CANCELLED order.");
        }

        paymentRepo.findByOrderId(orderId).ifPresent(p -> {
            if (p.getIsActive()) {
                throw new DuplicateResourceException("Payment", "orderId", String.valueOf(orderId));
            }
        });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(method);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus("PENDING");
        payment.setTransactionRef(HelperUtils.generateCode("TXN", 10));
        payment.setCreatedDate(LocalDateTime.now());
        payment.setUpdatedDate(LocalDateTime.now());
        payment.setIsActive(true);

        return PaymentResponseDTO.fromEntity(paymentRepo.save(payment));
    }

    @Transactional
    public PaymentResponseDTO completePayment(Integer paymentId) {
        Payment payment = findActivePayment(paymentId);
        if ("COMPLETED".equals(payment.getStatus())) {
            throw new InvalidOrderStateException("Payment is already COMPLETED.");
        }
        if ("REFUNDED".equals(payment.getStatus())) {
            throw new InvalidOrderStateException("Cannot complete a REFUNDED payment.");
        }
        payment.setStatus("COMPLETED");
        payment.setProcessedAt(LocalDateTime.now());
        payment.setUpdatedDate(LocalDateTime.now());
        return PaymentResponseDTO.fromEntity(paymentRepo.save(payment));
    }

    @Transactional
    public PaymentResponseDTO refundPayment(Integer orderId) {
        Payment payment = paymentRepo.findByOrderId(orderId)
                .filter(Payment::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", String.valueOf(orderId)));

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new InvalidOrderStateException(
                    "Only COMPLETED payments can be refunded. Current: " + payment.getStatus());
        }
        payment.setStatus("REFUNDED");
        payment.setUpdatedDate(LocalDateTime.now());
        return PaymentResponseDTO.fromEntity(paymentRepo.save(payment));
    }

    public PaymentResponseDTO getPaymentByOrderId(Integer orderId) {
        return PaymentResponseDTO.fromEntity(
                paymentRepo.findByOrderId(orderId)
                        .filter(Payment::getIsActive)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", String.valueOf(orderId))));
    }

    public PaymentResponseDTO getPaymentById(Integer paymentId) {
        return PaymentResponseDTO.fromEntity(findActivePayment(paymentId));
    }

    public Page<PaymentResponseDTO> getPaymentsFiltered(String method, String status,
                                                        LocalDateTime from, LocalDateTime to,
                                                        Pageable pageable) {
        return paymentRepo.findFiltered(method, status, from, to, pageable)
                .map(PaymentResponseDTO::fromEntity);
    }

    public List<Map<String, Object>> getAnalyticsByMethod() {
        return paymentRepo.sumAmountGroupedByMethod()
                .stream()
                .map(row -> Map.<String, Object>of(
                        "method", row[0],
                        "totalAmount", row[1]
                ))
                .collect(Collectors.toList());
    }

    private Payment findActivePayment(Integer id) {
        return paymentRepo.findById(id)
                .filter(Payment::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    private Order findActiveOrder(Integer id) {
        return orderRepo.findById(id)
                .filter(Order::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}