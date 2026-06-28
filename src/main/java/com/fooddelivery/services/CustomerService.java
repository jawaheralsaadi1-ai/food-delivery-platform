package com.fooddelivery.services;

import com.fooddelivery.dto.request.CustomerAddressRequestDTO;
import com.fooddelivery.dto.request.CustomerPatchDTO;
import com.fooddelivery.dto.request.CustomerRequestDTO;
import com.fooddelivery.dto.response.AddressResponseDTO;
import com.fooddelivery.dto.response.CustomerResponseDTO;
import com.fooddelivery.entities.Customer;
import com.fooddelivery.entities.CustomerAddress;
import com.fooddelivery.exceptions.DuplicateResourceException;
import com.fooddelivery.exceptions.ResourceNotFoundException;
import com.fooddelivery.repositories.CustomerAddressRepository;
import com.fooddelivery.repositories.CustomerRepository;
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
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final CustomerAddressRepository addressRepo;

    // ── Create (overloaded) ───────────────────────────────────────────────

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
        if (customerRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered: " + dto.getEmail());
        }
        Customer saved = customerRepo.save(dto.toEntity());
        return CustomerResponseDTO.fromEntity(saved);
    }

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto,
                                              CustomerAddressRequestDTO addressDto) {
        CustomerResponseDTO response = createCustomer(dto);
        Customer customer = findActiveEntityById(response.getId());
        CustomerAddress address = addressDto.toEntity();
        address.setCustomer(customer);
        address.setIsDefault(true);
        addressRepo.save(address);
        return response;
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public List<CustomerResponseDTO> findAll() {
        return customerRepo.findAllActive().stream()
                .map(CustomerResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CustomerResponseDTO findById(Integer id) {
        return CustomerResponseDTO.fromEntity(findActiveEntityById(id));
    }

    public CustomerResponseDTO findByEmail(String email) {
        Customer c = customerRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with email '" + email + "' not found"));
        return CustomerResponseDTO.fromEntity(c);
    }

    public Page<CustomerResponseDTO> searchByName(String name, Pageable pageable) {
        return customerRepo.searchByName(name, pageable)
                .map(CustomerResponseDTO::fromEntity);
    }

    // ── Address ───────────────────────────────────────────────────────────

    @Transactional
    public AddressResponseDTO addAddress(Integer customerId, CustomerAddressRequestDTO dto) {
        Customer customer = findActiveEntityById(customerId);
        CustomerAddress address = dto.toEntity();
        address.setCustomer(customer);
        return AddressResponseDTO.fromEntity(addressRepo.save(address));
    }

    public List<AddressResponseDTO> getAddresses(Integer customerId) {
        findActiveEntityById(customerId); // verify customer exists
        return addressRepo.findByCustomerId(customerId).stream()
                .map(AddressResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponseDTO setDefaultAddress(Integer addressId) {
        CustomerAddress address = addressRepo.findById(addressId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));

        // unset all current defaults for this customer
        addressRepo.findByCustomerId(address.getCustomer().getId())
                .forEach(a -> { a.setIsDefault(false); addressRepo.save(a); });

        address.setIsDefault(true);
        address.setUpdatedDate(LocalDateTime.now());
        return AddressResponseDTO.fromEntity(addressRepo.save(address));
    }

    @Transactional
    public void softDeleteAddress(Integer addressId) {
        CustomerAddress address = addressRepo.findById(addressId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        address.setIsActive(false);
        address.setUpdatedDate(LocalDateTime.now());
        addressRepo.save(address);
    }

    // ── Loyalty ───────────────────────────────────────────────────────────

    @Transactional
    public CustomerResponseDTO updateLoyaltyPoints(Integer customerId, int points) {
        Customer customer = findActiveEntityById(customerId);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customer.setUpdatedDate(LocalDateTime.now());
        return CustomerResponseDTO.fromEntity(customerRepo.save(customer));
    }

    @Transactional
    public CustomerResponseDTO applyLoyaltyPenalty(Integer customerId, int pointsDeducted) {
        Customer customer = findActiveEntityById(customerId);
        int newPoints = Math.max(0, customer.getLoyaltyPoints() - pointsDeducted);
        customer.setLoyaltyPoints(newPoints);
        customer.setUpdatedDate(LocalDateTime.now());
        return CustomerResponseDTO.fromEntity(customerRepo.save(customer));
    }

    // ── Patch ─────────────────────────────────────────────────────────────

    @Transactional
    public CustomerResponseDTO patchCustomer(Integer id, CustomerPatchDTO dto) {
        Customer customer = findActiveEntityById(id);
        if (dto.getFirstName() != null) customer.setFirstName(dto.getFirstName());
        if (dto.getLastName()  != null) customer.setLastName(dto.getLastName());
        if (dto.getPhone()     != null) customer.setPhone(dto.getPhone());
        customer.setUpdatedDate(LocalDateTime.now());
        return CustomerResponseDTO.fromEntity(customerRepo.save(customer));
    }

    // ── Soft Delete ───────────────────────────────────────────────────────

    @Transactional
    public void deactivateCustomer(Integer id) {
        Customer customer = findActiveEntityById(id);
        customer.setIsActive(false);
        customer.setUpdatedDate(LocalDateTime.now());
        customerRepo.save(customer);
    }

    // ── Top loyalty ───────────────────────────────────────────────────────

    public List<CustomerResponseDTO> getTopByLoyalty(int limit) {
        return customerRepo.findTopByLoyaltyPoints(limit).stream()
                .map(CustomerResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ── Internal helper ───────────────────────────────────────────────────

    public Customer findActiveEntityById(Integer id) {
        return customerRepo.findById(id)
                .filter(c -> c.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private ResourceNotFoundException notFound(String field, String val) {
        return new ResourceNotFoundException("Customer with " + field + " '" + val + "' not found");
    }
}