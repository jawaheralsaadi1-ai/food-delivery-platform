package com.fooddelivery.controllers;

import com.fooddelivery.dto.request.ComboMealRequestDTO;
import com.fooddelivery.dto.request.MenuItemRequestDTO;
import com.fooddelivery.dto.request.RestaurantRequestDTO;
import com.fooddelivery.dto.response.ComboMealResponseDTO;
import com.fooddelivery.dto.response.MenuItemResponseDTO;
import com.fooddelivery.dto.response.RestaurantResponseDTO;
import com.fooddelivery.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ── Core ──────────────────────────────────────────────────────────────

    @PostMapping("/owner/{ownerId}")
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(@PathVariable Integer ownerId,
                                                                  @Valid @RequestBody RestaurantRequestDTO dto) {
        RestaurantResponseDTO created = restaurantService.createRestaurant(dto, ownerId);
        return ResponseEntity.created(URI.create("/api/restaurants/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponseDTO>> getAll() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @GetMapping("/cuisine/{cuisine}")
    public ResponseEntity<List<RestaurantResponseDTO>> getByCuisine(@PathVariable String cuisine) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByCuisine(cuisine));
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponseDTO>> searchByKeyword(@RequestParam String keyword) {
        return ResponseEntity.ok(restaurantService.searchByKeyword(keyword));
    }

    @PutMapping("/{id}/toggle-orders")
    public ResponseEntity<RestaurantResponseDTO> toggleAcceptingOrders(@PathVariable Integer id,
                                                                       @RequestParam boolean accepting) {
        return ResponseEntity.ok(restaurantService.toggleAcceptingOrders(id, accepting));
    }

    @PutMapping("/{id}/fee/{newFee}")
    public ResponseEntity<RestaurantResponseDTO> updateDeliveryFee(@PathVariable Integer id,
                                                                   @PathVariable double newFee) {
        return ResponseEntity.ok(restaurantService.updateDeliveryFee(id, newFee));
    }

    // ── Menu ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenu(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getMenuForRestaurant(id));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<MenuItemResponseDTO> addMenuItem(@PathVariable Integer id,
                                                           @Valid @RequestBody MenuItemRequestDTO dto) {
        MenuItemResponseDTO created = restaurantService.addMenuItem(id, dto);
        return ResponseEntity.created(URI.create("/api/restaurants/" + id + "/menu/" + created.getId())).body(created);
    }

    @PutMapping("/menu/{itemId}/available")
    public ResponseEntity<MenuItemResponseDTO> toggleAvailability(@PathVariable Integer itemId,
                                                                  @RequestParam boolean status) {
        return ResponseEntity.ok(restaurantService.toggleMenuItemAvailability(itemId, status));
    }

    @PutMapping("/{id}/bulk-price-increase")
    public ResponseEntity<Void> bulkPriceIncrease(@PathVariable Integer id,
                                                  @RequestParam double percentage) {
        restaurantService.bulkUpdateMenuItemPrices(id, percentage);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/menu/top-sellers")
    public ResponseEntity<List<MenuItemResponseDTO>> topSellers(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getTopSellers(id));
    }

    @GetMapping("/menu/search")
    public ResponseEntity<List<MenuItemResponseDTO>> searchMenu(@RequestParam String keyword,
                                                                @RequestParam(required = false) Integer minCalories,
                                                                @RequestParam(required = false) Integer maxCalories) {
        return ResponseEntity.ok(restaurantService.searchMenuItems(keyword, minCalories, maxCalories));
    }

    // ── Combos ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/combos")
    public ResponseEntity<List<ComboMealResponseDTO>> getCombos(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getCombosForRestaurant(id));
    }

    @PostMapping("/{id}/combos")
    public ResponseEntity<ComboMealResponseDTO> createCombo(@PathVariable Integer id,
                                                            @Valid @RequestBody ComboMealRequestDTO dto) {
        ComboMealResponseDTO created = restaurantService.createCombo(id, dto);
        return ResponseEntity.created(URI.create("/api/restaurants/" + id + "/combos/" + created.getId())).body(created);
    }

    // ── Location & Analytics ──────────────────────────────────────────────

    @GetMapping("/near")
    public ResponseEntity<List<RestaurantResponseDTO>> getNearby(@RequestParam double lat,
                                                                 @RequestParam double lng,
                                                                 @RequestParam double radiusKm) {
        return ResponseEntity.ok(restaurantService.getNearby(lat, lng, radiusKm));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<Map<String, Object>> analytics(@PathVariable Integer id,
                                                         @RequestParam(required = false) String from,
                                                         @RequestParam(required = false) String to) {
        LocalDateTime fromDate = (from != null) ? LocalDateTime.parse(from + "T00:00:00") : LocalDateTime.now().minusYears(5);
        LocalDateTime toDate   = (to != null)   ? LocalDateTime.parse(to + "T23:59:59")   : LocalDateTime.now();
        return ResponseEntity.ok(restaurantService.getAnalytics(id, fromDate, toDate));
    }
}