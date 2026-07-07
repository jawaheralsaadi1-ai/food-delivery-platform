package com.fooddelivery.controllers;

import com.fooddelivery.dto.response.CustomerResponseDTO;
import com.fooddelivery.dto.response.DriverResponseDTO;
import com.fooddelivery.services.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    // ── Core (+ extended date-range variant merged into the same endpoint) ──

    @GetMapping("/revenue/restaurant/{restaurantId}")
    public ResponseEntity<Map<String, Object>> getRestaurantRevenue(
            @PathVariable Integer restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime dateTime = date != null ? date.atStartOfDay() : null;
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;
        return ResponseEntity.ok(reportingService.getRestaurantRevenue(restaurantId, dateTime, fromDateTime, toDateTime));
    }

    @GetMapping("/orders/count/restaurant/{restaurantId}")
    public ResponseEntity<Map<String, Object>> getRestaurantOrderCount(@PathVariable Integer restaurantId) {
        return ResponseEntity.ok(reportingService.getRestaurantOrderCount(restaurantId));
    }

    @GetMapping("/customers/top-loyalty")
    public ResponseEntity<List<CustomerResponseDTO>> getTopLoyaltyCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportingService.getTopLoyaltyCustomers(limit));
    }

    @GetMapping("/drivers/leaderboard")
    public ResponseEntity<List<DriverResponseDTO>> getDriverLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportingService.getDriverLeaderboard(limit));
    }

    @GetMapping("/platform/daily-summary")
    public ResponseEntity<Map<String, Object>> getPlatformDailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime dateTime = date != null ? date.atStartOfDay() : null;
        return ResponseEntity.ok(reportingService.getPlatformDailySummary(dateTime));
    }

    // ── Extended ──────────────────────────────────────────────────────────

    @GetMapping("/drivers/{driverId}/earnings")
    public ResponseEntity<Map<String, Object>> getDriverEarnings(
            @PathVariable Integer driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;
        return ResponseEntity.ok(reportingService.getDriverEarnings(driverId, fromDateTime, toDateTime));
    }

    @GetMapping("/orders/cancellation-rate")
    public ResponseEntity<Map<String, Object>> getCancellationRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;
        return ResponseEntity.ok(reportingService.getCancellationRate(fromDateTime, toDateTime));
    }

    @GetMapping("/platform/busiest-hours")
    public ResponseEntity<List<Map<String, Object>>> getBusiestHours() {
        return ResponseEntity.ok(reportingService.getBusiestHours());
    }
}