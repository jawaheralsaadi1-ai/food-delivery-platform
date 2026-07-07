package com.fooddelivery.controllers;

import com.fooddelivery.dto.response.PaymentResponseDTO;
import com.fooddelivery.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ── Core ──────────────────────────────────────────────────────────────

    @PostMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> createPayment(@PathVariable Integer orderId,
                                                            @RequestParam String method) {
        PaymentResponseDTO created = paymentService.processPayment(orderId, method);
        return ResponseEntity.created(URI.create("/api/payments/" + created.getId())).body(created);
    }

    @PutMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponseDTO> completePayment(@PathVariable Integer paymentId) {
        return ResponseEntity.ok(paymentService.completePayment(paymentId));
    }

    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(@PathVariable Integer paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    // ── Extended ──────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<PaymentResponseDTO>> getFilteredPayments(
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(paymentService.getPaymentsFiltered(method, status, fromDateTime, toDateTime, pageable));
    }

    @GetMapping("/analytics/by-method")
    public ResponseEntity<List<Map<String, Object>>> getAnalyticsByMethod() {
        return ResponseEntity.ok(paymentService.getAnalyticsByMethod());
    }
}