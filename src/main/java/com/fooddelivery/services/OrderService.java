package com.fooddelivery.services;

import com.fooddelivery.dto.request.CorporateOrderRequestDTO;
import com.fooddelivery.dto.request.OrderItemRequestDTO;
import com.fooddelivery.dto.response.CorporateOrderResponseDTO;
import com.fooddelivery.dto.response.OrderResponseDTO;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CustomerRepository customerRepo;
    private final RestaurantRepository restaurantRepo;
    private final MenuItemRepository menuItemRepo;
    private final CorporateOrderRepository corporateOrderRepo;
    private final CorporateOrderItemRepository corporateOrderItemRepo;

    private static final List<String> VALID_STATUSES =
            List.of("PENDING", "CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED");

    @Transactional
    public OrderResponseDTO createOrder(Integer customerId, Integer restaurantId,
                                        List<OrderItemRequestDTO> items) {
        return createOrder(customerId, restaurantId, items, null);
    }

    @Transactional
    public OrderResponseDTO createOrder(Integer customerId, Integer restaurantId,
                                        List<OrderItemRequestDTO> items, String notes) {
        Customer customer   = findActiveCustomer(customerId);
        Restaurant restaurant = findActiveRestaurant(restaurantId);

        if (!restaurant.getAcceptingOrders()) {
            throw new InvalidOrderStateException(
                    "Restaurant '" + restaurant.getName() + "' is not accepting orders right now.");
        }

        Order order = new Order();
        order.setOrderCode(HelperUtils.generateCode("ORD", 6));
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryNotes(notes);
        order.setDeliveryFee(restaurant.getDeliveryFee());
        order.setDiscountAmount(0.0);
        order.setSubtotal(0.0);
        order.setTotalAmount(0.0);
        order.setCreatedDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());
        order.setIsActive(true);

        Order savedOrder = orderRepo.save(order);

        for (OrderItemRequestDTO itemDTO : items) {
            persistOrderItem(savedOrder, itemDTO.getMenuItemId(),
                    itemDTO.getQuantity(), itemDTO.getSpecialInstructions());
        }

        recalculateTotals(savedOrder);
        return OrderResponseDTO.fromEntity(orderRepo.save(savedOrder));
    }

    @Transactional
    public OrderResponseDTO addMenuItemToOrder(Integer orderId, Integer menuItemId, int quantity) {
        Order order = findActiveOrder(orderId);
        guardEditable(order);
        persistOrderItem(order, menuItemId, quantity, null);
        recalculateTotals(order);
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO removeMenuItemFromOrder(Integer orderId, Integer orderItemId) {
        Order order = findActiveOrder(orderId);
        guardEditable(order);

        OrderItem item = orderItemRepo.findById(orderItemId)
                .filter(oi -> oi.getIsActive() && oi.getOrder().getId().equals(orderId))
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", orderItemId));

        item.setIsActive(false);
        item.setUpdatedDate(LocalDateTime.now());
        orderItemRepo.save(item);

        recalculateTotals(order);
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO applyDiscount(Integer orderId, double discountAmount) {
        Order order = findActiveOrder(orderId);
        guardEditable(order);
        order.setDiscountAmount(discountAmount);
        recalculateTotals(order);
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Integer orderId, String newStatus) {
        Order order = findActiveOrder(orderId);
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new InvalidOrderStateException("Invalid order status: '" + newStatus + "'.");
        }
        order.setStatus(newStatus);
        order.setUpdatedDate(LocalDateTime.now());
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO confirmOrder(Integer orderId) {
        Order order = findActiveOrder(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Only PENDING orders can be confirmed. Current status: " + order.getStatus());
        }
        order.setStatus("CONFIRMED");
        order.setUpdatedDate(LocalDateTime.now());
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Integer orderId) {
        Order order = findActiveOrder(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Order can only be cancelled when PENDING. Current status: " + order.getStatus());
        }
        order.setStatus("CANCELLED");
        order.setUpdatedDate(LocalDateTime.now());
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public OrderResponseDTO calculateOrderTotals(Integer orderId) {
        Order order = findActiveOrder(orderId);
        recalculateTotals(order);
        return OrderResponseDTO.fromEntity(orderRepo.save(order));
    }

    @Transactional
    public CorporateOrderResponseDTO placeCorporateOrder(CorporateOrderRequestDTO dto) {
        Restaurant restaurant = findActiveRestaurant(dto.getRestaurantId());

        CorporateOrder corporateOrder = new CorporateOrder();
        corporateOrder.setCorporateCode(HelperUtils.generateCode("CORP", 8));
        corporateOrder.setCompanyName(dto.getCompanyName());
        corporateOrder.setCostCenter(dto.getCostCenter());
        corporateOrder.setRestaurant(restaurant);
        corporateOrder.setStatus("PENDING");
        corporateOrder.setOrderDate(LocalDateTime.now());
        corporateOrder.setTotalAmount(0.0);
        corporateOrder.setCreatedDate(LocalDateTime.now());
        corporateOrder.setUpdatedDate(LocalDateTime.now());
        corporateOrder.setIsActive(true);

        CorporateOrder saved = corporateOrderRepo.save(corporateOrder);
        double total = 0.0;

        for (OrderItemRequestDTO itemDTO : dto.getItems()) {
            MenuItem menuItem = findActiveMenuItem(itemDTO.getMenuItemId());

            CorporateOrderItem item = new CorporateOrderItem();
            item.setCorporateOrder(saved);
            item.setMenuItem(menuItem);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(menuItem.getPrice());
            item.setTotalPrice(menuItem.getPrice() * itemDTO.getQuantity());
            item.setSpecialInstructions(itemDTO.getSpecialInstructions());
            item.setCreatedDate(LocalDateTime.now());
            item.setUpdatedDate(LocalDateTime.now());
            item.setIsActive(true);
            corporateOrderItemRepo.save(item);
            total += item.getTotalPrice();
        }

        saved.setTotalAmount(total);
        saved.setUpdatedDate(LocalDateTime.now());
        return CorporateOrderResponseDTO.fromEntity(corporateOrderRepo.save(saved));
    }

    @Transactional
    public OrderResponseDTO reorder(Integer orderId) {
        Order original = findActiveOrder(orderId);
        List<OrderItem> originalItems = orderItemRepo.findByOrderId(original.getId());

        Order newOrder = new Order();
        newOrder.setOrderCode(HelperUtils.generateCode("ORD", 6));
        newOrder.setCustomer(original.getCustomer());
        newOrder.setRestaurant(original.getRestaurant());
        newOrder.setStatus("PENDING");
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setDeliveryFee(original.getRestaurant().getDeliveryFee());
        newOrder.setDiscountAmount(0.0);
        newOrder.setSubtotal(0.0);
        newOrder.setTotalAmount(0.0);
        newOrder.setCreatedDate(LocalDateTime.now());
        newOrder.setUpdatedDate(LocalDateTime.now());
        newOrder.setIsActive(true);
        Order saved = orderRepo.save(newOrder);

        for (OrderItem oi : originalItems) {
            persistOrderItem(saved, oi.getMenuItem().getId(), oi.getQuantity(), oi.getSpecialInstructions());
        }

        recalculateTotals(saved);
        return OrderResponseDTO.fromEntity(orderRepo.save(saved));
    }

    public OrderResponseDTO getOrderById(Integer orderId) {
        return OrderResponseDTO.fromEntity(findActiveOrder(orderId));
    }

    public List<OrderResponseDTO> getOrdersByRestaurantAndStatus(Integer restaurantId, String status) {
        return orderRepo.findByRestaurantIdAndStatus(restaurantId, status)
                .stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<OrderResponseDTO> getOrdersByCustomer(Integer customerId,
                                                      String status,
                                                      LocalDateTime from,
                                                      LocalDateTime to,
                                                      Pageable pageable) {
        return orderRepo.findByCustomerFiltered(customerId, status, from, to, pageable)
                .map(OrderResponseDTO::fromEntity);
    }

    public double getEstimatedDeliveryTime(Integer orderId) {
        Order order = findActiveOrder(orderId);
        if (order.getDelivery() == null || order.getDelivery().getDriver() == null) {
            throw new InvalidOrderStateException("No driver assigned to this order yet.");
        }
        DeliveryDriver driver     = order.getDelivery().getDriver();
        CustomerAddress address   = order.getCustomer().getAddresses()
                .stream()
                .filter(CustomerAddress::getIsDefault)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Default address for customer", order.getCustomer().getId()));

        double distanceKm = HelperUtils.calculateDistance(
                driver.getCurrentLat(), driver.getCurrentLng(),
                address.getLatitude(), address.getLongitude());

        double averageSpeedKmh = 30.0;
        return (distanceKm / averageSpeedKmh) * 60;
    }

    private void persistOrderItem(Order order, Integer menuItemId,
                                  int quantity, String specialInstructions) {
        MenuItem menuItem = findActiveMenuItem(menuItemId);
        if (!menuItem.getRestaurant().getId().equals(order.getRestaurant().getId())) {
            throw new InvalidOrderStateException(
                    "MenuItem " + menuItemId + " does not belong to restaurant " + order.getRestaurant().getId());
        }
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setMenuItem(menuItem);
        item.setQuantity(quantity);
        item.setUnitPrice(menuItem.getPrice());
        item.setTotalPrice(menuItem.getPrice() * quantity);
        item.setSpecialInstructions(specialInstructions);
        item.setCreatedDate(LocalDateTime.now());
        item.setUpdatedDate(LocalDateTime.now());
        item.setIsActive(true);
        orderItemRepo.save(item);
    }

    private void recalculateTotals(Order order) {
        double subtotal = orderItemRepo.findByOrderId(order.getId())
                .stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
        order.setSubtotal(subtotal);
        order.setTotalAmount(
                HelperUtils.calculateTotal(subtotal, order.getDeliveryFee(), order.getDiscountAmount()));
        order.setUpdatedDate(LocalDateTime.now());
    }

    private void guardEditable(Order order) {
        if (!"PENDING".equals(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Order cannot be modified in status: " + order.getStatus() + ". Only PENDING orders are editable.");
        }
    }

    private Order findActiveOrder(Integer id) {
        return orderRepo.findById(id)
                .filter(Order::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private Customer findActiveCustomer(Integer id) {
        return customerRepo.findById(id)
                .filter(Customer::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private Restaurant findActiveRestaurant(Integer id) {
        return restaurantRepo.findById(id)
                .filter(Restaurant::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
    }

    private MenuItem findActiveMenuItem(Integer id) {
        return menuItemRepo.findById(id)
                .filter(m -> m.getIsActive() && m.getIsAvailable())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }
}