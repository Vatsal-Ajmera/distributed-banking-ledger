package com.bank.customer.service;

import com.bank.customer.dto.CustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.entity.Customer;
import com.bank.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(request.getPassword()) // still raw (fix next commit)
                .build();

        Customer saved = customerRepository.save(customer);

        return mapToResponse(saved);
    }

    public Optional<CustomerResponse> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .map(this::mapToResponse);
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
