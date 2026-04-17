package com.bank.customer.service;

import com.bank.customer.dto.CustomerRegistrationRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.dto.CustomerUpdateRequest;

import java.util.UUID;

/**
 * Service interface for customer operations.
 */
public interface CustomerService {

    CustomerResponse register(CustomerRegistrationRequest request);

    CustomerResponse getById(UUID id);

    CustomerResponse getByEmail(String email);

    CustomerResponse update(UUID id, CustomerUpdateRequest request);

    void delete(UUID id);
}
