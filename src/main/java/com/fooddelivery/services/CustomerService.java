package com.fooddelivery.services;

import com.fooddelivery.dto.request.CustomerAddressRequestDTO;
import com.fooddelivery.dto.request.CustomerPatchDTO;
import com.fooddelivery.dto.request.CustomerRequestDTO;
import com.fooddelivery.dto.response.CustomerAddressResponseDTO;
import com.fooddelivery.dto.response.CustomerResponseDTO;
import com.fooddelivery.entities.Customer;
import com.fooddelivery.entities.CustomerAddress;
import com.fooddelivery.exceptions.DuplicateResourceException;
import com.fooddelivery.exceptions.ResourceNotFoundException;
import com.fooddelivery.repositories.CustomerAddressRepository;
import com.fooddelivery.repositories.CustomerRepository;
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
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final CustomerAddressRepository customerAddressRepo;

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {
        customerRepo.findByEmail(dto.getEmail()).ifPresent(existing -> {
            throw new DuplicateResourceException("Customer", "email", dto.getEmail());
        });

        Customer customer = dto.toEntity();
        customer.setCustomerCode(HelperUtils.generateCode("CUST"));
        customer.setLoyaltyPoints(0);
        customer.setCreatedDate(LocalDateTime.now());
        customer.setUpdatedDate(LocalDateTime.now());
        customer.setIsActive(true);

        return CustomerResponseDTO.fromEntity(customerRepo.save(customer));
    }

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto, CustomerAddressRequestDTO initialAddress) {
        CustomerResponseDTO saved = createCustomer(dto);

        Customer customer = customerRepo.findById(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", saved.getId()));

        CustomerAddress address = initialAddress.toEntity();
        address.setCustomer(customer);
        address.setIsDefault(true);
        address.setCreatedDate(LocalDateTime.now());
        address.setUpdatedDate(LocalDateTime.now());
        address.setIsActive(true);
        customerAddressRepo.save(address);

        return CustomerResponseDTO.fromEntity(
                customerRepo.findById(customer.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", customer.getId())));
    }

    @Transactional
    public CustomerAddressResponseDTO addAddress(Integer customerId, CustomerAddressRequestDTO dto) {
        Customer customer = findActiveCustomerById(customerId);

        CustomerAddress address = dto.toEntity();
        address.setCustomer(customer);
        address.setIsDefault(false);
        address.setCreatedDate(LocalDateTime.now());
        address.setUpdatedDate(LocalDateTime.now());
        address.setIsActive(true);

        return CustomerAddressResponseDTO.fromEntity(customerAddressRepo.save(address));
    }

    @Transactional
    public CustomerResponseDTO updateLoyaltyPoints(Integer customerId, int points) {
        Customer customer = findActiveCustomerById(customerId);
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customer.setUpdatedDate(LocalDateTime.now());
        return CustomerResponseDTO.fromEntity(customerRepo.save(customer));
    }

    @Transactional
    public CustomerResponseDTO applyLoyaltyPenalty(Integer customerId, int pointsDeducted) {
        Customer customer = findActiveCustomerById(customerId);
        customer.setLoyaltyPoints(Math.max(customer.getLoyaltyPoints() - pointsDeducted, 0));
        customer.setUpdatedDate(LocalDateTime.now());
        return CustomerResponseDTO.fromEntity(customerRepo.save(customer));
    }

    @Transactional
    public void deactivateCustomer(Integer customerId) {
        Customer customer = findActiveCustomerById(customerId);
        customer.setIsActive(false);
        customer.setUpdatedDate(LocalDateTime.now());
        customerRepo.save(customer);
    }

    @Transactional
    public CustomerResponseDTO patchCustomer(Integer customerId, CustomerPatchDTO dto) {
        Customer customer = findActiveCustomerById(customerId);
        if (dto.getPhone() != null)     customer.setPhone(dto.getPhone());
        if (dto.getFirstName() != null) customer.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)  customer.setLastName(dto.getLastName());
        customer.setUpdatedDate(LocalDateTime.now());
        return CustomerResponseDTO.fromEntity(customerRepo.save(customer));
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepo.findAllActive()
                .stream()
                .map(CustomerResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CustomerResponseDTO getCustomerById(Integer id) {
        return CustomerResponseDTO.fromEntity(findActiveCustomerById(id));
    }

    public CustomerResponseDTO getCustomerByEmail(String email) {
        Customer customer = customerRepo.findByEmail(email)
                .filter(Customer::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return CustomerResponseDTO.fromEntity(customer);
    }

    public Page<CustomerResponseDTO> searchCustomersByName(String name, Pageable pageable) {
        return customerRepo.searchByName(name, pageable)
                .map(CustomerResponseDTO::fromEntity);
    }

    public List<CustomerAddressResponseDTO> getAddressesForCustomer(Integer customerId) {
        findActiveCustomerById(customerId);
        return customerAddressRepo.findByCustomerId(customerId)
                .stream()
                .map(CustomerAddressResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerAddressResponseDTO setDefaultAddress(Integer addressId) {
        CustomerAddress address = customerAddressRepo.findById(addressId)
                .filter(CustomerAddress::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", addressId));

        customerAddressRepo.findByCustomerId(address.getCustomer().getId())
                .stream()
                .filter(CustomerAddress::getIsDefault)
                .forEach(a -> {
                    a.setIsDefault(false);
                    a.setUpdatedDate(LocalDateTime.now());
                    customerAddressRepo.save(a);
                });

        address.setIsDefault(true);
        address.setUpdatedDate(LocalDateTime.now());
        return CustomerAddressResponseDTO.fromEntity(customerAddressRepo.save(address));
    }

    @Transactional
    public void deactivateAddress(Integer addressId) {
        CustomerAddress address = customerAddressRepo.findById(addressId)
                .filter(CustomerAddress::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", addressId));
        address.setIsActive(false);
        address.setUpdatedDate(LocalDateTime.now());
        customerAddressRepo.save(address);
    }

    private Customer findActiveCustomerById(Integer id) {
        return customerRepo.findById(id)
                .filter(Customer::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}