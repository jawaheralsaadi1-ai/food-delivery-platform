package com.fooddelivery.controllers;

import com.fooddelivery.dto.response.DeliveryResponseDTO;
import com.fooddelivery.dto.response.DriverPerformanceDTO;
import com.fooddelivery.dto.response.DriverResponseDTO;
import com.fooddelivery.services.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    // ── Core ──────────────────────────────────────────────────────────────

    @PostMapping("/order/{orderId}/assign-manual/{driverId}")
    public ResponseEntity<DeliveryResponseDTO> assignManual(@PathVariable Integer orderId, @PathVariable Integer driverId) {
        DeliveryResponseDTO created = deliveryService.assignDriverToOrder(orderId, driverId);
        return ResponseEntity.created(URI.create("/api/deliveries/" + created.getId())).body(created);
    }

    @PostMapping("/order/{orderId}/assign-auto")
    public ResponseEntity<DeliveryResponseDTO> assignAuto(@PathVariable Integer orderId) {
        DeliveryResponseDTO created = deliveryService.autoAssignDriver(orderId);
        return ResponseEntity.created(URI.create("/api/deliveries/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryById(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    @PutMapping("/{id}/pickup")
    public ResponseEntity<DeliveryResponseDTO> markPickedUp(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.markDeliveryPickedUp(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<DeliveryResponseDTO> markCompleted(@PathVariable Integer id) {
        return ResponseEntity.ok(deliveryService.markDeliveryDelivered(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByStatus(status));
    }

    // ── Extended ──────────────────────────────────────────────────────────

    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<DriverResponseDTO>> getNearbyDrivers(@RequestParam double lat,
                                                                    @RequestParam double lng,
                                                                    @RequestParam double radiusKm) {
        return ResponseEntity.ok(deliveryService.getNearbyOnlineDrivers(lat, lng, radiusKm));
    }

    @GetMapping("/drivers/{driverId}/performance")
    public ResponseEntity<DriverPerformanceDTO> getDriverPerformance(@PathVariable Integer driverId) {
        return ResponseEntity.ok(deliveryService.getDriverPerformance(driverId));
    }
}