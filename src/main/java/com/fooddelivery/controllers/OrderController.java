package com.fooddelivery.controllers;

import com.fooddelivery.dto.request.CorporateOrderRequestDTO;
import com.fooddelivery.dto.request.OrderItemRequestDTO;
import com.fooddelivery.dto.response.CorporateOrderResponseDTO;
import com.fooddelivery.dto.response.OrderEtaResponseDTO;
import com.fooddelivery.dto.response.OrderResponseDTO;
import com.fooddelivery.dto.response.OrderTimelineEventDTO;
import com.fooddelivery.services.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    // ── Core ──────────────────────────────────────────────────────────────

    @PostMapping("/customer/{customerId}/restaurant/{restaurantId}")
    public ResponseEntity<OrderResponseDTO> initializeOrder(@PathVariable Integer customerId,
                                                            @PathVariable Integer restaurantId) {
        OrderResponseDTO created = orderService.createOrder(customerId, restaurantId, List.of());
        return ResponseEntity.created(URI.create("/api/orders/" + created.getId())).body(created);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponseDTO> addItem(@PathVariable Integer id,
                                                    @Valid @RequestBody OrderItemRequestDTO dto) {
        int quantity = dto.getQuantity() != null ? dto.getQuantity() : 1;
        OrderResponseDTO updated = orderService.addMenuItemToOrder(id, dto.getMenuItemId(), quantity);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Integer id, @PathVariable Integer itemId) {
        orderService.removeMenuItemFromOrder(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/discount/{amount}")
    public ResponseEntity<OrderResponseDTO> applyDiscount(@PathVariable Integer id, @PathVariable double amount) {
        return ResponseEntity.ok(orderService.applyDiscount(id, amount));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderResponseDTO> confirmOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Integer id,
            @PathVariable
            @Pattern(
                    regexp = "PENDING|CONFIRMED|PREPARING|READY|OUT_FOR_DELIVERY|DELIVERED|CANCELLED",
                    message = "Status must be one of: PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED")
            String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForRestaurant(@PathVariable Integer restaurantId,
                                                                         @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getOrdersForRestaurant(restaurantId, status));
    }

    @PostMapping("/corporate")
    public ResponseEntity<CorporateOrderResponseDTO> placeCorporateOrder(@Valid @RequestBody CorporateOrderRequestDTO dto) {
        CorporateOrderResponseDTO created = orderService.placeCorporateOrder(dto);
        return ResponseEntity.created(URI.create("/api/orders/corporate/" + created.getId())).body(created);
    }

    // ── Extended ──────────────────────────────────────────────────────────

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<OrderTimelineEventDTO>> getTimeline(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderTimeline(id));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<OrderResponseDTO> reorder(@PathVariable Integer id) {
        OrderResponseDTO created = orderService.reorder(id);
        return ResponseEntity.created(URI.create("/api/orders/" + created.getId())).body(created);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersForCustomerFiltered(
            @PathVariable Integer customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getOrdersForCustomerFiltered(customerId, status, fromDateTime, toDateTime, pageable));
    }

    @GetMapping("/{id}/eta")
    public ResponseEntity<OrderEtaResponseDTO> getEta(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getEta(id));
    }
}