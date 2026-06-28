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
}
