package com.fooddelivery.services;

import com.fooddelivery.dto.response.CustomerResponseDTO;
import com.fooddelivery.dto.response.DriverResponseDTO;
import com.fooddelivery.repositories.CustomerRepository;
import com.fooddelivery.repositories.DeliveryDriverRepository;
import com.fooddelivery.repositories.DeliveryRepository;
import com.fooddelivery.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final DeliveryDriverRepository driverRepo;
    private final DeliveryRepository deliveryRepo;

    // ── Core: GET /api/reports/revenue/restaurant/{restaurantId}?date=... ──
    // ── Extended: GET /api/reports/revenue/restaurant/{restaurantId}?from=&to= ──
    // Both variants share one controller method; this service method branches
    // on whether a date-range or a single date was supplied.
    public Map<String, Object> getRestaurantRevenue(Integer restaurantId, LocalDateTime date,
                                                    LocalDateTime from, LocalDateTime to) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("restaurantId", restaurantId);

        if (from != null || to != null) {
            LocalDateTime start = from != null ? from : LocalDateTime.of(1970, 1, 1, 0, 0);
            LocalDateTime end = to != null ? to : LocalDateTime.now();
            Double revenue = orderRepo.sumRevenueByRestaurantAndDateRange(restaurantId, start, end);
            result.put("from", start);
            result.put("to", end);
            result.put("revenue", revenue != null ? revenue : 0.0);
        } else {
            LocalDateTime targetDate = date != null ? date : LocalDateTime.now();
            Double revenue = orderRepo.sumDeliveredRevenueByRestaurantAndDate(restaurantId, targetDate);
            result.put("date", targetDate.toLocalDate());
            result.put("revenue", revenue != null ? revenue : 0.0);
        }
        return result;
    }

    // GET /api/reports/orders/count/restaurant/{restaurantId} — total lifetime orders
    public Map<String, Object> getRestaurantOrderCount(Integer restaurantId) {
        Long count = orderRepo.countCompletedOrdersByRestaurantId(restaurantId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("restaurantId", restaurantId);
        result.put("totalCompletedOrders", count != null ? count : 0L);
        return result;
    }

    // GET /api/reports/customers/top-loyalty — top N customers by loyalty points
    public List<CustomerResponseDTO> getTopLoyaltyCustomers(int limit) {
        return customerRepo.findTopByLoyaltyPoints(limit).stream()
                .map(CustomerResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // GET /api/reports/drivers/leaderboard — top drivers by completed deliveries
    public List<DriverResponseDTO> getDriverLeaderboard(int limit) {
        return driverRepo.findTopDriversByCompletedDeliveries(limit).stream()
                .map(DriverResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // GET /api/reports/platform/daily-summary?date=YYYY-MM-DD
    public Map<String, Object> getPlatformDailySummary(LocalDateTime date) {
        LocalDateTime targetDate = date != null ? date : LocalDateTime.now();
        Long totalOrders = orderRepo.countDeliveredOnDate(targetDate);
        Double totalDeliveryFees = orderRepo.sumDeliveryFeesOnDate(targetDate);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", targetDate.toLocalDate());
        result.put("totalDeliveredOrders", totalOrders != null ? totalOrders : 0L);
        result.put("totalDeliveryFeesCollected", totalDeliveryFees != null ? totalDeliveryFees : 0.0);
        return result;
    }

    // Extended: GET /api/reports/drivers/{driverId}/earnings?from=&to=
    public Map<String, Object> getDriverEarnings(Integer driverId, LocalDateTime from, LocalDateTime to) {
        LocalDateTime start = from != null ? from : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime end = to != null ? to : LocalDateTime.now();
        Double earnings = deliveryRepo.sumEarningsByDriverAndDateRange(driverId, start, end);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("driverId", driverId);
        result.put("from", start);
        result.put("to", end);
        result.put("totalEarnings", earnings != null ? earnings : 0.0);
        return result;
    }

    // Extended: GET /api/reports/orders/cancellation-rate?from=&to=
    public Map<String, Object> getCancellationRate(LocalDateTime from, LocalDateTime to) {
        LocalDateTime start = from != null ? from : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime end = to != null ? to : LocalDateTime.now();

        Long cancelled = orderRepo.countCancelledBetween(start, end);
        Long total = orderRepo.countTotalBetween(start, end);
        long cancelledCount = cancelled != null ? cancelled : 0L;
        long totalCount = total != null ? total : 0L;
        double rate = totalCount > 0 ? (double) cancelledCount / totalCount : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", start);
        result.put("to", end);
        result.put("cancelledOrders", cancelledCount);
        result.put("totalOrders", totalCount);
        result.put("cancellationRate", Math.round(rate * 10000.0) / 10000.0);
        return result;
    }

    // Extended: GET /api/reports/platform/busiest-hours
    public List<Map<String, Object>> getBusiestHours() {
        return orderRepo.findOrderVolumeByHour().stream()
                .map(row -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("hour", row[0]);
                    entry.put("orderCount", row[1]);
                    return entry;
                })
                .collect(Collectors.toList());
    }
}