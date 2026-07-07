package com.fooddelivery.controllers;

import com.fooddelivery.dto.request.CustomerAddressRequestDTO;
import com.fooddelivery.dto.request.CustomerPatchDTO;
import com.fooddelivery.dto.request.CustomerRequestDTO;
import com.fooddelivery.dto.response.CustomerAddressResponseDTO;
import com.fooddelivery.dto.response.CustomerResponseDTO;
import com.fooddelivery.dto.response.OrderResponseDTO;
import com.fooddelivery.services.CustomerService;
import com.fooddelivery.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    // ── Core ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO dto) {
        CustomerResponseDTO created = customerService.createCustomer(dto);
        return ResponseEntity.created(URI.create("/api/customers/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponseDTO> getCustomerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCustomer(@PathVariable Integer id) {
        customerService.deactivateCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/loyalty/add/{points}")
    public ResponseEntity<CustomerResponseDTO> addLoyaltyPoints(@PathVariable Integer id, @PathVariable int points) {
        return ResponseEntity.ok(customerService.updateLoyaltyPoints(id, points));
    }

    @PutMapping("/{id}/loyalty/deduct/{points}")
    public ResponseEntity<CustomerResponseDTO> deductLoyaltyPoints(@PathVariable Integer id, @PathVariable int points) {
        return ResponseEntity.ok(customerService.applyLoyaltyPenalty(id, points));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<CustomerAddressResponseDTO> addAddress(@PathVariable Integer id,
                                                                 @Valid @RequestBody CustomerAddressRequestDTO dto) {
        CustomerAddressResponseDTO created = customerService.addAddress(id, dto);
        return ResponseEntity.created(URI.create("/api/customers/" + id + "/addresses/" + created.getId())).body(created);
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<CustomerAddressResponseDTO>> getAddresses(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getAddressesForCustomer(id));
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<CustomerAddressResponseDTO> setDefaultAddress(@PathVariable Integer addressId) {
        return ResponseEntity.ok(customerService.setDefaultAddress(addressId));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId) {
        customerService.deactivateAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    // GET /{id}/orders — plain list when no filters/pagination requested (core behaviour);
    // becomes paginated + filterable the moment status/from/to/page/size are supplied (extended behaviour).
    @GetMapping("/{id}/orders")
    public ResponseEntity<?> getOrdersForCustomer(
            @PathVariable Integer id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        boolean wantsFiltering = status != null || from != null || to != null || page != null || size != null;
        if (!wantsFiltering) {
            return ResponseEntity.ok(orderService.getOrdersForCustomer(id));
        }

        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 10);
        return ResponseEntity.ok(orderService.getOrdersForCustomerFiltered(id, status, fromDateTime, toDateTime, pageable));
    }

    // ── Extended ──────────────────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponseDTO>> searchByName(@RequestParam String name,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerService.searchCustomersByName(name, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> patchCustomer(@PathVariable Integer id,
                                                             @Valid @RequestBody CustomerPatchDTO dto) {
        return ResponseEntity.ok(customerService.patchCustomer(id, dto));
    }
}