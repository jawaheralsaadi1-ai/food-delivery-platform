
package com.fooddelivery.services;

import com.fooddelivery.dto.request.ComboMealRequestDTO;
import com.fooddelivery.dto.request.MenuItemRequestDTO;
import com.fooddelivery.dto.request.RestaurantRequestDTO;
import com.fooddelivery.dto.response.*;
import com.fooddelivery.entities.*;
import com.fooddelivery.exceptions.ResourceNotFoundException;
import com.fooddelivery.repositories.*;
import com.fooddelivery.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepo;
    private final RestaurantOwnerRepository ownerRepo;
    private final MenuItemRepository menuItemRepo;
    private final ComboMealRepository comboMealRepo;
    private final ReviewRepository reviewRepo;
    private final OrderRepository orderRepo;

    // ── Create

    @Transactional
    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO dto, Integer ownerId) {
        RestaurantOwner owner = ownerRepo.findActiveById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("RestaurantOwner", ownerId));
        Restaurant restaurant = dto.toEntity();
        restaurant.setOwner(owner);
        return RestaurantResponseDTO.fromEntity(restaurantRepo.save(restaurant));
    }

    // ── Read

    public List<RestaurantResponseDTO> findAll() {
        return restaurantRepo.findAllActive().stream()
                .map(RestaurantResponseDTO::fromEntity).collect(Collectors.toList());
    }

    public RestaurantResponseDTO findById(Integer id) {
        return RestaurantResponseDTO.fromEntity(findActiveEntityById(id));
    }

    public List<RestaurantResponseDTO> getRestaurantsByCuisine(String cuisine) {
        return restaurantRepo.findByCuisineTypeIgnoreCase(cuisine).stream()
                .map(RestaurantResponseDTO::fromEntity).collect(Collectors.toList());
    }

    public List<RestaurantResponseDTO> getRestaurantsUnderDeliveryFee(double maxFee) {
        return restaurantRepo.findByDeliveryFeeLessThanEqual(maxFee).stream()
                .map(RestaurantResponseDTO::fromEntity).collect(Collectors.toList());
    }

    public List<RestaurantResponseDTO> getNearby(double lat, double lng, double radiusKm) {
        return restaurantRepo.findAllActive().stream()
                .filter(r -> {
                    // restaurants don't have lat/lng in spec; filter by acceptance as proxy
                    // In a real system you'd store lat/lng on Restaurant
                    return r.getAcceptingOrders();
                })
                .map(RestaurantResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponseDTO> searchByKeyword(String keyword) {
        return restaurantRepo.findByNameKeyword(keyword).stream()
                .map(RestaurantResponseDTO::fromEntity).collect(Collectors.toList());
    }

    // ── Toggle / Update ───────────────────────────────────────────────────

    @Transactional
    public RestaurantResponseDTO toggleAcceptingOrders(Integer id, boolean status) {
        Restaurant r = findActiveEntityById(id);
        r.setAcceptingOrders(status);
        r.setUpdatedDate(LocalDateTime.now());
        return RestaurantResponseDTO.fromEntity(restaurantRepo.save(r));
    }

    @Transactional
    public RestaurantResponseDTO updateDeliveryFee(Integer id, double newFee) {
        Restaurant r = findActiveEntityById(id);
        r.setDeliveryFee(newFee);
        r.setUpdatedDate(LocalDateTime.now());
        return RestaurantResponseDTO.fromEntity(restaurantRepo.save(r));
    }

    // ── Menu ──────────────────────────────────────────────────────────────

    public List<MenuItemResponseDTO> getMenuForRestaurant(Integer restaurantId) {
        findActiveEntityById(restaurantId);
        return menuItemRepo.findByRestaurantId(restaurantId).stream()
                .map(MenuItemResponseDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponseDTO addMenuItem(Integer restaurantId, MenuItemRequestDTO dto) {
        Restaurant r = findActiveEntityById(restaurantId);
        MenuItem item = dto.toEntity();
        item.setRestaurant(r);
        return MenuItemResponseDTO.fromEntity(menuItemRepo.save(item));
    }

    @Transactional
    public MenuItemResponseDTO toggleMenuItemAvailability(Integer itemId, boolean status) {
        MenuItem item = menuItemRepo.findActiveById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        item.setIsAvailable(status);
        item.setUpdatedDate(LocalDateTime.now());
        return MenuItemResponseDTO.fromEntity(menuItemRepo.save(item));
    }

    @Transactional
    public void bulkUpdateMenuItemPrices(Integer restaurantId, double percentageIncrease) {
        findActiveEntityById(restaurantId);
        List<MenuItem> items = menuItemRepo.findByRestaurantId(restaurantId);
        double multiplier = 1 + (percentageIncrease / 100.0);
        items.forEach(item -> {
            item.setPrice(Math.round(item.getPrice() * multiplier * 100.0) / 100.0);
            item.setUpdatedDate(LocalDateTime.now());
            menuItemRepo.save(item);
        });
    }

    public List<MenuItemResponseDTO> getTopSellers(Integer restaurantId) {
        findActiveEntityById(restaurantId);
        return menuItemRepo.findTopSellersByRestaurantId(restaurantId).stream()
                .map(MenuItemResponseDTO::fromEntity).collect(Collectors.toList());
    }

    public List<MenuItemResponseDTO> searchMenuItems(String keyword,
                                                     Integer minCalories,
                                                     Integer maxCalories) {
        if (minCalories != null && maxCalories != null) {
            return menuItemRepo.searchByKeywordAndCalories(keyword, minCalories, maxCalories)
                    .stream().map(MenuItemResponseDTO::fromEntity).collect(Collectors.toList());
        }
        return menuItemRepo.searchByKeyword(keyword).stream()
                .map(MenuItemResponseDTO::fromEntity).collect(Collectors.toList());
    }

    // ── Combos ────────────────────────────────────────────────────────────

    public List<ComboMealResponseDTO> getCombosForRestaurant(Integer restaurantId) {
        findActiveEntityById(restaurantId);
        return comboMealRepo.findByRestaurantId(restaurantId).stream()
                .map(ComboMealResponseDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public ComboMealResponseDTO createCombo(Integer restaurantId, ComboMealRequestDTO dto) {
        Restaurant r = findActiveEntityById(restaurantId);
        ComboMeal combo = dto.toEntity();
        combo.setRestaurant(r);
        List<MenuItem> items = dto.getMenuItemIds().stream()
                .map(id -> menuItemRepo.findActiveById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id)))
                .collect(Collectors.toList());
        combo.setMenuItems(items);
        return ComboMealResponseDTO.fromEntity(comboMealRepo.save(combo));
    }

    // ── Analytics ─────────────────────────────────────────────────────────

    public Map<String, Object> getAnalytics(Integer restaurantId) {
        findActiveEntityById(restaurantId);
        Double avgRating   = reviewRepo.avgRatingByRestaurantId(restaurantId);
        Long   totalOrders = orderRepo.countCompletedOrdersByRestaurantId(restaurantId);
        Double revenue     = orderRepo.sumRevenueByRestaurantAndDateRange(
                restaurantId,
                LocalDateTime.now().minusYears(10),
                LocalDateTime.now());
        return Map.of(
                "restaurantId", restaurantId,
                "averageRating", avgRating != null ? avgRating : 0.0,
                "totalCompletedOrders", totalOrders != null ? totalOrders : 0L,
                "totalRevenue", revenue != null ? revenue : 0.0
        );
    }

    // ── Internal ──────────────────────────────────────────────────────────

    public Restaurant findActiveEntityById(Integer id) {
        return restaurantRepo.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
    }
}