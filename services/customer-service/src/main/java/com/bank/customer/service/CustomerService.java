package com.bank.customer.service;

import com.bank.customer.dto.CustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.entity.Customer;
import com.bank.customer.exception.CustomerNotFoundException;
import com.bank.customer.exception.DuplicateCustomerException;
import com.bank.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final Map<String, CustomerResponse> idempotencyStore = new HashMap<>();

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, String idempotencyKey) {

        log.info("Creating customer with email: {}", request.getEmail());

        // Idempotency check
        if (idempotencyStore.containsKey(idempotencyKey)) {
            log.warn("Duplicate request detected for idempotency key: {}", idempotencyKey);
            return idempotencyStore.get(idempotencyKey);
        }

        // Business rule
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.error("Duplicate customer creation attempt for email: {}", request.getEmail());
            throw new DuplicateCustomerException(request.getEmail());
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        Customer saved = customerRepository.save(customer);

        log.info("Customer created successfully with id: {}", saved.getId());

        CustomerResponse response = mapToResponse(saved);

        idempotencyStore.put(idempotencyKey, response);

        return response;
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {

        log.info("Fetching customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", email);
                    return new CustomerNotFoundException(email);
                });

        return mapToResponse(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .kycStatus(customer.getKycStatus())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
