package com.bank.customer.service;

import com.bank.customer.dto.CustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.entity.Customer;
import com.bank.customer.exception.CustomerNotFoundException;
import com.bank.customer.repository.CustomerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Idempotency store (simple version)
    private final Map<String, CustomerResponse> idempotencyStore = new HashMap<>();

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request, String idempotencyKey) {

        // 🔑 Check if already processed
        if (idempotencyStore.containsKey(idempotencyKey)) {
            return idempotencyStore.get(idempotencyKey);
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        Customer saved = customerRepository.save(customer);

        CustomerResponse response = mapToResponse(saved);

        // 🔑 Store result
        idempotencyStore.put(idempotencyKey, response);

        return response;
    }

    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(email));

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
