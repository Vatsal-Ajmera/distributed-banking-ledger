package com.bank.customer.service;

import com.bank.customer.dto.CustomerRegistrationRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.dto.CustomerUpdateRequest;
import com.bank.customer.entity.Customer;
import com.bank.customer.exception.CustomerNotFoundException;
import com.bank.customer.exception.DuplicateEmailException;
import com.bank.customer.mapper.CustomerMapper;
import com.bank.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link CustomerService}.
 * <p>
 * Sensitive data (email, password) is never logged.
 * Timestamps (updatedAt) are managed exclusively by JPA/Hibernate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CustomerResponse register(CustomerRegistrationRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (customerRepository.existsByEmail(normalizedEmail)) {
            log.warn("Duplicate registration attempt");
            throw new DuplicateEmailException("A customer with this email already exists");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        // Plaintext password is discarded after this point — never logged or persisted

        Customer customer = CustomerMapper.toEntity(request, passwordHash);
        Customer saved = customerRepository.save(customer);

        log.info("Customer registered id={}", saved.getId());

        return CustomerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        Customer customer = customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found id={}", id);
                    return new CustomerNotFoundException("No customer found with id " + id);
                });

        return CustomerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        Customer customer = customerRepository.findByEmailAndActiveTrue(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Customer not found for email lookup");
                    return new CustomerNotFoundException("No customer found with the provided email");
                });

        return CustomerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(UUID id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found id={}", id);
                    return new CustomerNotFoundException("No customer found with id " + id);
                });

        customer.setName(request.getName().trim());
        Customer updated = customerRepository.save(customer);

        log.info("Customer updated id={}", updated.getId());

        return CustomerMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        customerRepository.findByIdAndActiveTrue(id).ifPresentOrElse(
                customer -> {
                    customer.setActive(false);
                    customerRepository.save(customer);
                    log.info("Customer soft-deleted id={}", id);
                },
                () -> log.info("Customer already inactive or not found id={}, DELETE is no-op", id)
        );
        // Always idempotent — returns successfully regardless
    }
}
