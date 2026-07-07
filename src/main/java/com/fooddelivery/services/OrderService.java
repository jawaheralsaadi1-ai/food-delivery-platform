package com.fooddelivery.services;

import com.fooddelivery.dto.request.CorporateOrderRequestDTO;
import com.fooddelivery.dto.request.OrderItemRequestDTO;
import com.fooddelivery.dto.response.CorporateOrderResponseDTO;
import com.fooddelivery.dto.response.OrderEtaResponseDTO;
import com.fooddelivery.dto.response.OrderResponseDTO;
import com.fooddelivery.dto.response.OrderTimelineEventDTO;
import com.fooddelivery.entities.*;
import com.fooddelivery.exceptions.InvalidOrderStateException;
import com.fooddelivery.exceptions.ResourceNotFoundException;
import com.fooddelivery.repositories.*;
import com.fooddelivery.utils.HelperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "PENDING", "CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED");
    private static final Set<String> TERMINAL_STATUSES = Set.of("DELIVERED", "CANCELLED");
    private static final double AVERAGE_DELIVERY_SPEED_KMH = 30.0;

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CustomerRepository customerRepo;
    private final RestaurantRepository restaurantRepo;
    private final MenuItemRepository menuItemRepo;
    private final CorporateOrderRepository corporateOrderRepo;
    private final CorporateOrderItemRepository corporateOrderItemRepo;
    private final DeliveryRepository deliveryRepo;

    // ── Create ────────────────────────────────────────────────────────────

    @Transactional
    public OrderResponseDTO createOrder(Integer customerId, Integer restaurantId, List<OrderItemRequestDTO> items) {
        return createOrder(customerId, restaurantId, items, null);
    }

    @Transactional
    public OrderResponseDTO createOrder(Integer customerId, Integer restaurantId,
                                        List<OrderItemRequestDTO> items, String notes) {
        Customer customer = findActiveCustomer(customerId);
        Restaurant restaurant = restaurantRepo.findActiveById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));

        if (!Boolean.TRUE.equals(restaurant.getAcceptingOrders())) {
            throw new InvalidOrderStateException("Restaurant '" + restaurant.getName() + "' is not currently accepting orders.");
        }

        Order order = Order.builder()
                .orderCode(HelperUtils.generateCode("ORD"))
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .subtotal(0.0)
                .deliveryFee(restaurant.getDeliveryFee())
                .discountAmount(0.0)
                .totalAmount(0.0)
                .deliveryNotes(notes)
                .customer(customer)
                .restaurant(restaurant)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .isActive(true)
                .build();

        order = orderRepo.save(order);

        if (items != null) {
            for (OrderItemRequestDTO itemDto : items) {
                attachItem(order, itemDto);
            }
        }

        Order recalculated = recalcTotals(order.getId());
        return OrderResponseDTO.fromEntity(recalculated);
    }

    // ── Item management ──────────────────────────────────────────────────

    @Transactional
    public OrderResponseDTO addMenuItemToOrder(Integer orderId, Integer menuItemId, int quantity) {
        Order order = findActiveOrder(orderId);
        requireModifiable(order);

        OrderItemRequestDTO itemDto = new OrderItemRequestDTO();
        itemDto.setMenuItemId(menuItemId);
        itemDto.setQuantity(quantity);
        attachItem(order, itemDto);

        return OrderResponseDTO.fromEntity(recalcTotals(orderId));
    }

    @Transactional
    public void removeMenuItemFromOrder(Integer orderId, Integer orderItemId) {
        Order order = findActiveOrder(orderId);
        requireModifiable(order);

        OrderItem item = orderItemRepo.findActiveById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", orderItemId));

        if (!item.getOrder().getId().equals(orderId)) {
            throw new ResourceNotFoundException("OrderItem", orderItemId);
        }

        item.setIsActive(false);
        item.setUpdatedDate(LocalDateTime.now());
        orderItemRepo.save(item);

        recalcTotals(orderId);
    }

    // ── State transitions ────────────────────────────────────────────────

    @Transactional
    public OrderResponseDTO applyDiscount(Integer orderId, double discountAmount) {
        Order order = findActiveOrder(orderId);
        requireModifiable(order);

        double safeDiscount = Math.max(discountAmount, 0.0);
        order.setDiscountAmount(safeDiscount);
        order.setTotalAmount(HelperUtils.calculateTotal(order.getSubtotal(), order.getDeliveryFee(), safeDiscount));
        order.setUpdatedDate(LocalDateTime.now());

        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO confirmOrder(Integer orderId) {
        Order order = findActiveOrder(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Order", order.getId(), order.getStatus(), "PENDING");
        }
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        if (items.isEmpty()) {
            throw new InvalidOrderStateException("Cannot confirm an order with no items.");
        }
        order.setStatus("CONFIRMED");
        order.setUpdatedDate(LocalDateTime.now());
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Integer orderId, String newStatus) {
        Order order = findActiveOrder(orderId);

        String normalized = newStatus == null ? "" : newStatus.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalized)) {
            throw new InvalidOrderStateException("'" + newStatus + "' is not a recognized order status.");
        }
        if (TERMINAL_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStateException("Order", order.getId(), order.getStatus(), "a non-terminal status");
        }

        order.setStatus(normalized);
        order.setUpdatedDate(LocalDateTime.now());
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Integer orderId) {
        Order order = findActiveOrder(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Order", order.getId(), order.getStatus(), "PENDING");
        }
        order.setStatus("CANCELLED");
        order.setUpdatedDate(LocalDateTime.now());
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO calculateOrderTotals(Integer orderId) {
        return OrderResponseDTO.fromEntity(recalcTotals(orderId));
    }

    // ── Corporate orders ─────────────────────────────────────────────────

    @Transactional
    public CorporateOrderResponseDTO placeCorporateOrder(CorporateOrderRequestDTO dto) {
        Restaurant restaurant = restaurantRepo.findActiveById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", dto.getRestaurantId()));

        CorporateOrder corporateOrder = CorporateOrder.builder()
                .corporateCode(HelperUtils.generateCode("CORP"))
                .companyName(dto.getCompanyName())
                .costCenter(dto.getCostCenter())
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .totalAmount(0.0)
                .restaurant(restaurant)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .isActive(true)
                .build();

        corporateOrder = corporateOrderRepo.save(corporateOrder);

        double total = 0.0;
        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            MenuItem menuItem = menuItemRepo.findActiveById(itemDto.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemDto.getMenuItemId()));

            double unitPrice = menuItem.getPrice();
            double lineTotal = unitPrice * itemDto.getQuantity();
            total += lineTotal;

            CorporateOrderItem corpItem = CorporateOrderItem.builder()
                    .quantity(itemDto.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(lineTotal)
                    .specialInstructions(itemDto.getSpecialInstructions())
                    .corporateOrder(corporateOrder)
                    .menuItem(menuItem)
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .isActive(true)
                    .build();
            corporateOrderItemRepo.save(corpItem);
        }

        corporateOrder.setTotalAmount(total);
        corporateOrder.setUpdatedDate(LocalDateTime.now());
        corporateOrder = corporateOrderRepo.save(corporateOrder);

        return CorporateOrderResponseDTO.fromEntity(refetchCorporateOrder(corporateOrder.getId()));
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public OrderResponseDTO getOrderById(Integer orderId) {
        return OrderResponseDTO.fromEntity(findActiveOrder(orderId));
    }

    public List<OrderResponseDTO> getOrdersForCustomer(Integer customerId) {
        findActiveCustomer(customerId);
        return orderRepo.findByCustomerId(customerId).stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<OrderResponseDTO> getOrdersForCustomerFiltered(Integer customerId, String status,
                                                               LocalDateTime from, LocalDateTime to,
                                                               Pageable pageable) {
        findActiveCustomer(customerId);
        return orderRepo.findByCustomerFiltered(customerId, status, from, to, pageable)
                .map(OrderResponseDTO::fromEntity);
    }

    public List<OrderResponseDTO> getOrdersForRestaurant(Integer restaurantId, String status) {
        List<Order> orders = (status == null || status.isBlank())
                ? orderRepo.findByRestaurantId(restaurantId)
                : orderRepo.findByRestaurantIdAndStatus(restaurantId, status.toUpperCase());
        return orders.stream().map(OrderResponseDTO::fromEntity).collect(Collectors.toList());
    }

    public List<OrderTimelineEventDTO> getOrderTimeline(Integer orderId) {
        Order order = findActiveOrder(orderId);
        List<OrderTimelineEventDTO> timeline = new ArrayList<>();

        timeline.add(OrderTimelineEventDTO.builder()
                .status("PENDING")
                .timestamp(order.getCreatedDate())
                .note("Order placed by customer.")
                .build());

        deliveryRepo.findByOrderId(orderId).ifPresent(delivery -> {
            if (delivery.getAssignedAt() != null) {
                timeline.add(OrderTimelineEventDTO.builder()
                        .status("DRIVER_ASSIGNED")
                        .timestamp(delivery.getAssignedAt())
                        .note("Driver assigned for delivery.")
                        .build());
            }
            if (delivery.getPickedUpAt() != null) {
                timeline.add(OrderTimelineEventDTO.builder()
                        .status("OUT_FOR_DELIVERY")
                        .timestamp(delivery.getPickedUpAt())
                        .note("Order picked up by driver.")
                        .build());
            }
            if (delivery.getDeliveredAt() != null) {
                timeline.add(OrderTimelineEventDTO.builder()
                        .status("DELIVERED")
                        .timestamp(delivery.getDeliveredAt())
                        .note("Order delivered to customer.")
                        .build());
            }
        });

        boolean currentStatusRepresented = timeline.stream()
                .anyMatch(e -> e.getStatus().equals(order.getStatus()));
        if (!currentStatusRepresented) {
            timeline.add(OrderTimelineEventDTO.builder()
                    .status(order.getStatus())
                    .timestamp(order.getUpdatedDate())
                    .note("Current order status.")
                    .build());
        }

        timeline.sort((a, b) -> {
            if (a.getTimestamp() == null) return -1;
            if (b.getTimestamp() == null) return 1;
            return a.getTimestamp().compareTo(b.getTimestamp());
        });

        return timeline;
    }

    public OrderEtaResponseDTO getEta(Integer orderId) {
        Order order = findActiveOrder(orderId);

        return deliveryRepo.findByOrderId(orderId)
                .map(delivery -> {
                    DeliveryDriver driver = delivery.getDriver();
                    Restaurant restaurant = order.getRestaurant();

                    if (driver == null || driver.getCurrentLat() == null || driver.getCurrentLng() == null
                            || restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
                        return OrderEtaResponseDTO.builder()
                                .orderId(order.getId())
                                .orderStatus(order.getStatus())
                                .driverAssigned(true)
                                .driverId(driver != null ? driver.getId() : null)
                                .message("Driver or restaurant location unavailable; cannot compute ETA yet.")
                                .build();
                    }

                    double distanceKm = HelperUtils.calculateDistance(
                            driver.getCurrentLat(), driver.getCurrentLng(),
                            restaurant.getLatitude(), restaurant.getLongitude());
                    int etaMinutes = (int) Math.ceil((distanceKm / AVERAGE_DELIVERY_SPEED_KMH) * 60.0);

                    return OrderEtaResponseDTO.builder()
                            .orderId(order.getId())
                            .orderStatus(order.getStatus())
                            .driverAssigned(true)
                            .driverId(driver.getId())
                            .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                            .etaMinutes(etaMinutes)
                            .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(etaMinutes))
                            .build();
                })
                .orElseGet(() -> OrderEtaResponseDTO.builder()
                        .orderId(order.getId())
                        .orderStatus(order.getStatus())
                        .driverAssigned(false)
                        .message("No driver has been assigned to this order yet.")
                        .build());
    }

    @Transactional
    public OrderResponseDTO reorder(Integer orderId) {
        Order original = findActiveOrder(orderId);
        List<OrderItem> originalItems = orderItemRepo.findByOrderId(orderId);

        if (originalItems.isEmpty()) {
            throw new InvalidOrderStateException("Cannot reorder an order with no items.");
        }

        List<OrderItemRequestDTO> items = originalItems.stream().map(oi -> {
            OrderItemRequestDTO dto = new OrderItemRequestDTO();
            dto.setMenuItemId(oi.getMenuItem().getId());
            dto.setQuantity(oi.getQuantity());
            dto.setSpecialInstructions(oi.getSpecialInstructions());
            return dto;
        }).collect(Collectors.toList());

        return createOrder(original.getCustomer().getId(), original.getRestaurant().getId(),
                items, original.getDeliveryNotes());
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private void attachItem(Order order, OrderItemRequestDTO itemDto) {
        MenuItem menuItem = menuItemRepo.findActiveById(itemDto.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemDto.getMenuItemId()));

        int quantity = itemDto.getQuantity() != null ? itemDto.getQuantity() : 1;
        double unitPrice = menuItem.getPrice();

        OrderItem orderItem = OrderItem.builder()
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(unitPrice * quantity)
                .specialInstructions(itemDto.getSpecialInstructions())
                .order(order)
                .menuItem(menuItem)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .isActive(true)
                .build();

        orderItemRepo.save(orderItem);
    }

    private Order recalcTotals(Integer orderId) {
        Order order = findActiveOrder(orderId);
        double subtotal = orderItemRepo.findByOrderId(orderId).stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();

        order.setSubtotal(subtotal);
        order.setTotalAmount(HelperUtils.calculateTotal(subtotal, order.getDeliveryFee(), order.getDiscountAmount()));
        order.setUpdatedDate(LocalDateTime.now());
        return orderRepo.save(order);
    }

    private void requireModifiable(Order order) {
        if (!"PENDING".equals(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Order " + order.getId() + " is locked and can no longer be modified (status: " + order.getStatus() + ").");
        }
    }

    private CorporateOrder refetchCorporateOrder(Integer id) {
        return corporateOrderRepo.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CorporateOrder", id));
    }

    private Order findActiveOrder(Integer id) {
        return orderRepo.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private Customer findActiveCustomer(Integer id) {
        return customerRepo.findById(id)
                .filter(Customer::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}