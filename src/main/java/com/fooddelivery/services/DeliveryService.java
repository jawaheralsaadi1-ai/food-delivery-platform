package com.fooddelivery.services;

import com.fooddelivery.dto.request.DriverRequestDTO;
import com.fooddelivery.dto.response.DeliveryResponseDTO;
import com.fooddelivery.dto.response.DriverPerformanceDTO;
import com.fooddelivery.dto.response.DriverResponseDTO;
import com.fooddelivery.entities.Delivery;
import com.fooddelivery.entities.DeliveryDriver;
import com.fooddelivery.entities.Order;
import com.fooddelivery.exceptions.DuplicateResourceException;
import com.fooddelivery.exceptions.InvalidOrderStateException;
import com.fooddelivery.exceptions.ResourceNotFoundException;
import com.fooddelivery.repositories.DeliveryDriverRepository;
import com.fooddelivery.repositories.DeliveryRepository;
import com.fooddelivery.repositories.OrderRepository;
import com.fooddelivery.repositories.ReviewRepository;
import com.fooddelivery.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepo;
    private final DeliveryDriverRepository driverRepo;
    private final OrderRepository orderRepo;
    private final ReviewRepository reviewRepo;

    // ── Driver registration & management ─────────────────────────────────
    // (registerDriver is not one of the 7 methods literally named in the Day 4.5
    // spec table, but DriverController's "POST / Register new driver" endpoint
    // has to live somewhere, and Driver management belongs to this Epic/service.)

    @Transactional
    public DriverResponseDTO registerDriver(DriverRequestDTO dto) {
        driverRepo.findByEmail(dto.getEmail()).ifPresent(existing -> {
            throw new DuplicateResourceException("DeliveryDriver", "email", dto.getEmail());
        });

        DeliveryDriver driver = dto.toEntity();
        driver.setCreatedDate(LocalDateTime.now());
        driver.setUpdatedDate(LocalDateTime.now());
        driver.setIsActive(true);

        return DriverResponseDTO.fromEntity(driverRepo.save(driver));
    }

    public List<DriverResponseDTO> getAllDrivers() {
        return driverRepo.findAllActive().stream()
                .map(DriverResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DriverResponseDTO> getOnlineDrivers() {
        return driverRepo.findAllOnline().stream()
                .map(DriverResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Required method #7: toggleDriverOnlineStatus ─────────────────────
    @Transactional
    public DriverResponseDTO toggleDriverOnlineStatus(Integer driverId, boolean isOnline) {
        DeliveryDriver driver = findActiveDriver(driverId);
        driver.setIsOnline(isOnline);
        driver.setUpdatedDate(LocalDateTime.now());
        return DriverResponseDTO.fromEntity(driverRepo.save(driver));
    }

    // ── Required method #3: updateDriverLocation ──────────────────────────
    @Transactional
    public DriverResponseDTO updateDriverLocation(Integer driverId, double lat, double lng) {
        DeliveryDriver driver = findActiveDriver(driverId);
        driver.setCurrentLat(lat);
        driver.setCurrentLng(lng);
        driver.setUpdatedDate(LocalDateTime.now());
        return DriverResponseDTO.fromEntity(driverRepo.save(driver));
    }

    // Extended: GET /api/deliveries/drivers/nearby
    public List<DriverResponseDTO> getNearbyOnlineDrivers(double lat, double lng, double radiusKm) {
        return driverRepo.findAllOnline().stream()
                .filter(d -> d.getCurrentLat() != null && d.getCurrentLng() != null)
                .filter(d -> HelperUtils.calculateDistance(lat, lng, d.getCurrentLat(), d.getCurrentLng()) <= radiusKm)
                .map(DriverResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Extended: GET /api/deliveries/drivers/{driverId}/performance
    public DriverPerformanceDTO getDriverPerformance(Integer driverId) {
        DeliveryDriver driver = findActiveDriver(driverId);
        Long completed = deliveryRepo.countCompletedByDriverId(driver.getId());
        Double avgTime = deliveryRepo.avgDeliveryTimeByDriverId(driver.getId());
        Double avgRating = reviewRepo.avgRatingByDriverId(driver.getId());

        return DriverPerformanceDTO.builder()
                .driverId(driver.getId())
                .completedDeliveries(completed != null ? completed : 0L)
                .averageDeliveryTimeMinutes(avgTime != null ? avgTime : 0.0)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .build();
    }

    // ── Delivery lifecycle ────────────────────────────────────────────────

    // ── Required method #1: assignDriverToOrder ───────────────────────────
    @Transactional
    public DeliveryResponseDTO assignDriverToOrder(Integer orderId, Integer driverId) {
        Order order = findActiveOrder(orderId);
        DeliveryDriver driver = findActiveDriver(driverId);

        if (!Boolean.TRUE.equals(driver.getIsOnline())) {
            throw new InvalidOrderStateException("Driver " + driverId + " is not online and cannot be assigned a delivery.");
        }
        if (!"READY".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Order", order.getId(), order.getStatus(), "READY");
        }
        deliveryRepo.findByOrderId(orderId).ifPresent(existing -> {
            throw new DuplicateResourceException("Delivery", "orderId", String.valueOf(orderId));
        });

        Delivery delivery = createDelivery(order, driver);
        return DeliveryResponseDTO.fromEntity(delivery);
    }

    // ── Required method #2: autoAssignDriver — first available online driver ──
    @Transactional
    public DeliveryResponseDTO autoAssignDriver(Integer orderId) {
        Order order = findActiveOrder(orderId);

        if (!"READY".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Order", order.getId(), order.getStatus(), "READY");
        }
        deliveryRepo.findByOrderId(orderId).ifPresent(existing -> {
            throw new DuplicateResourceException("Delivery", "orderId", String.valueOf(orderId));
        });

        DeliveryDriver driver = driverRepo.findFirstOnlineDriver()
                .orElseThrow(() -> new ResourceNotFoundException("No online DeliveryDriver is currently available."));

        Delivery delivery = createDelivery(order, driver);
        return DeliveryResponseDTO.fromEntity(delivery);
    }

    // ── Required method #4: markDeliveryPickedUp ──────────────────────────
    @Transactional
    public DeliveryResponseDTO markDeliveryPickedUp(Integer deliveryId) {
        Delivery delivery = findActiveDelivery(deliveryId);
        if (!"ASSIGNED".equals(delivery.getStatus())) {
            throw new InvalidOrderStateException("Delivery " + deliveryId + " must be ASSIGNED before pickup (current: "
                    + delivery.getStatus() + ").");
        }
        delivery.setStatus("PICKED_UP");
        delivery.setPickedUpAt(LocalDateTime.now());
        delivery.setUpdatedDate(LocalDateTime.now());
        delivery = deliveryRepo.save(delivery);

        Order order = delivery.getOrder();
        order.setStatus("OUT_FOR_DELIVERY");
        order.setUpdatedDate(LocalDateTime.now());
        orderRepo.save(order);

        return DeliveryResponseDTO.fromEntity(delivery);
    }

    // ── Required method #5: markDeliveryDelivered ─────────────────────────
    @Transactional
    public DeliveryResponseDTO markDeliveryDelivered(Integer deliveryId) {
        Delivery delivery = findActiveDelivery(deliveryId);
        if ("DELIVERED".equals(delivery.getStatus())) {
            throw new InvalidOrderStateException("Delivery " + deliveryId + " has already been delivered.");
        }
        delivery.setStatus("DELIVERED");
        delivery.setDeliveredAt(LocalDateTime.now());
        delivery.setUpdatedDate(LocalDateTime.now());
        delivery = deliveryRepo.save(delivery);

        Order order = delivery.getOrder();
        order.setStatus("DELIVERED");
        order.setUpdatedDate(LocalDateTime.now());
        orderRepo.save(order);

        return DeliveryResponseDTO.fromEntity(delivery);
    }

    // ── Required method #6: getDeliveriesForDriver ────────────────────────
    public List<DeliveryResponseDTO> getDeliveriesForDriver(Integer driverId, String status) {
        findActiveDriver(driverId);
        List<Delivery> deliveries = (status == null || status.isBlank())
                ? deliveryRepo.findByDriverId(driverId)
                : deliveryRepo.findByDeliveryDriverIdAndStatus(driverId, status.toUpperCase());
        return deliveries.stream().map(DeliveryResponseDTO::fromEntity).collect(Collectors.toList());
    }

    public DeliveryResponseDTO getActiveDeliveryForDriver(Integer driverId) {
        findActiveDriver(driverId);
        Delivery delivery = deliveryRepo.findActiveDeliveryByDriverId(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Active delivery for driver " + driverId + " not found."));
        return DeliveryResponseDTO.fromEntity(delivery);
    }

    public DeliveryResponseDTO getDeliveryById(Integer deliveryId) {
        return DeliveryResponseDTO.fromEntity(findActiveDelivery(deliveryId));
    }

    public List<DeliveryResponseDTO> getDeliveriesByStatus(String status) {
        return deliveryRepo.findByStatus(status.toUpperCase()).stream()
                .map(DeliveryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Extended: GET /api/reports/drivers/{driverId}/earnings
    public double getDriverEarnings(Integer driverId, LocalDateTime from, LocalDateTime to) {
        findActiveDriver(driverId);
        Double sum = deliveryRepo.sumEarningsByDriverAndDateRange(driverId, from, to);
        return sum != null ? sum : 0.0;
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private Delivery createDelivery(Order order, DeliveryDriver driver) {
        Delivery delivery = Delivery.builder()
                .trackingCode(HelperUtils.generateCode("TRK"))
                .status("ASSIGNED")
                .assignedAt(LocalDateTime.now())
                .order(order)
                .driver(driver)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .isActive(true)
                .build();

        return deliveryRepo.save(delivery);
    }

    private Order findActiveOrder(Integer id) {
        return orderRepo.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private DeliveryDriver findActiveDriver(Integer id) {
        return driverRepo.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryDriver", id));
    }

    private Delivery findActiveDelivery(Integer id) {
        return deliveryRepo.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
    }
}