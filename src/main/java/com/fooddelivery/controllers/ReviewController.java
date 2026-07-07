package com.fooddelivery.controllers;

import com.fooddelivery.dto.request.ReviewRequestDTO;
import com.fooddelivery.dto.response.ReviewResponseDTO;
import com.fooddelivery.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ── Core ──────────────────────────────────────────────────────────────

    @PostMapping("/restaurant/{restaurantId}/customer/{customerId}")
    public ResponseEntity<ReviewResponseDTO> submitRestaurantReview(@PathVariable Integer restaurantId,
                                                                    @PathVariable Integer customerId,
                                                                    @Valid @RequestBody ReviewRequestDTO dto) {
        ReviewResponseDTO created = reviewService.leaveRestaurantReview(customerId, restaurantId, dto.getRating(), dto.getComment());
        return ResponseEntity.created(URI.create("/api/reviews/" + created.getId())).body(created);
    }

    @PostMapping("/driver/{driverId}/customer/{customerId}")
    public ResponseEntity<ReviewResponseDTO> submitDriverReview(@PathVariable Integer driverId,
                                                                @PathVariable Integer customerId,
                                                                @Valid @RequestBody ReviewRequestDTO dto) {
        ReviewResponseDTO created = reviewService.leaveDriverReview(customerId, driverId, dto.getRating(), dto.getComment());
        return ResponseEntity.created(URI.create("/api/reviews/" + created.getId())).body(created);
    }

    // GET /restaurant/{restaurantId} — plain list by default; becomes paginated
    // the moment page/size query params are supplied (core + extended combined).
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> getReviewsForRestaurant(@PathVariable Integer restaurantId,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(reviewService.getReviewsForRestaurant(restaurantId));
        }
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10);
        return ResponseEntity.ok(reviewService.getReviewsForRestaurantPaged(restaurantId, pageable));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsForDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(reviewService.getReviewsForDriver(driverId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
        reviewService.deactivateReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    // ── Extended ──────────────────────────────────────────────────────────

    @GetMapping("/restaurant/{restaurantId}/average")
    public ResponseEntity<Map<String, Object>> getRestaurantAverageRating(@PathVariable Integer restaurantId) {
        double avg = reviewService.getAverageRatingForRestaurant(restaurantId);
        return ResponseEntity.ok(Map.of("restaurantId", restaurantId, "averageRating", avg));
    }

    @GetMapping("/driver/{driverId}/average")
    public ResponseEntity<Map<String, Object>> getDriverAverageRating(@PathVariable Integer driverId) {
        double avg = reviewService.getAverageRatingForDriver(driverId);
        return ResponseEntity.ok(Map.of("driverId", driverId, "averageRating", avg));
    }
}