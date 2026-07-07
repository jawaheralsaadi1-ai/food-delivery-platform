package com.fooddelivery.services;

import com.fooddelivery.dto.response.ReviewResponseDTO;
import com.fooddelivery.entities.Customer;
import com.fooddelivery.entities.DeliveryDriver;
import com.fooddelivery.entities.Restaurant;
import com.fooddelivery.entities.Review;
import com.fooddelivery.exceptions.DuplicateResourceException;
import com.fooddelivery.exceptions.ResourceNotFoundException;
import com.fooddelivery.repositories.CustomerRepository;
import com.fooddelivery.repositories.DeliveryDriverRepository;
import com.fooddelivery.repositories.RestaurantRepository;
import com.fooddelivery.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final CustomerRepository customerRepo;
    private final RestaurantRepository restaurantRepo;
    private final DeliveryDriverRepository driverRepo;

    @Transactional
    public ReviewResponseDTO leaveRestaurantReview(Integer customerId, Integer restaurantId,
                                                   int rating, String comment) {
        Customer customer     = findActiveCustomer(customerId);
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .filter(Restaurant::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));

        Review review = buildReview(customer, rating, comment, "RESTAURANT");
        review.setRestaurant(restaurant);
        review.setDriver(null);

        return ReviewResponseDTO.fromEntity(reviewRepo.save(review));
    }

    @Transactional
    public ReviewResponseDTO leaveDriverReview(Integer customerId, Integer driverId,
                                               int rating, String comment) {
        Customer customer     = findActiveCustomer(customerId);
        DeliveryDriver driver = driverRepo.findById(driverId)
                .filter(DeliveryDriver::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryDriver", driverId));


        reviewRepo.findByDriverId(driverId)
                .stream()
                .filter(r -> r.getCustomer().getId().equals(customerId) && r.getIsActive())
                .findFirst()
                .ifPresent(r -> {
                    throw new DuplicateResourceException("Review", "customerId/driverId", customerId + "-" + driverId);
                });

        Review review = buildReview(customer, rating, comment, "DRIVER");
        review.setRestaurant(null);
        review.setDriver(driver);

        return ReviewResponseDTO.fromEntity(reviewRepo.save(review));
    }

    public List<ReviewResponseDTO> getReviewsForRestaurant(Integer restaurantId) {
        return reviewRepo.findByRestaurantId(restaurantId)
                .stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<ReviewResponseDTO> getReviewsForRestaurantPaged(Integer restaurantId, Pageable pageable) {
        return reviewRepo.findByRestaurantIdPageable(restaurantId, pageable)
                .map(ReviewResponseDTO::fromEntity);
    }

    public List<ReviewResponseDTO> getReviewsForDriver(Integer driverId) {
        return reviewRepo.findByDriverId(driverId)
                .stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public double getAverageRatingForRestaurant(Integer restaurantId) {
        Double avg = reviewRepo.avgRatingByRestaurantId(restaurantId);
        return avg != null ? avg : 0.0;
    }

    public double getAverageRatingForDriver(Integer driverId) {
        Double avg = reviewRepo.avgRatingByDriverId(driverId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public void deactivateReview(Integer reviewId) {
        Review review = reviewRepo.findActiveById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        review.setIsActive(false);
        review.setUpdatedDate(LocalDateTime.now());
        reviewRepo.save(review);
    }

    private Review buildReview(Customer customer, int rating, String comment, String targetType) {
        Review review = new Review();
        review.setCustomer(customer);
        review.setRating(rating);
        review.setComment(comment);
        review.setTargetType(targetType);
        review.setCreatedAt(LocalDateTime.now());
        review.setCreatedDate(LocalDateTime.now());
        review.setUpdatedDate(LocalDateTime.now());
        review.setIsActive(true);
        return review;
    }

    private Customer findActiveCustomer(Integer id) {
        return customerRepo.findById(id)
                .filter(Customer::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}