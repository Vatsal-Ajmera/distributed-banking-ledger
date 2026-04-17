package com.bank.customer.mapper;

import com.bank.customer.dto.CustomerRegistrationRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.entity.Customer;

/**
 * Maps between Customer entity and DTOs.
 */
public final class CustomerMapper {

    private CustomerMapper() {
        // utility class
    }

    /**
     * Converts a registration request DTO to a Customer entity.
     * Email is trimmed and lowercased. Password hash is injected separately.
     *
     * @param request      the registration request DTO
     * @param passwordHash the BCrypt hash of the plaintext password
     * @return a new Customer entity (not yet persisted)
     */
    public static Customer toEntity(CustomerRegistrationRequest request, String passwordHash) {
        return Customer.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordHash)
                .build();
    }

    /**
     * Converts a Customer entity to the outbound response DTO.
     * Never exposes passwordHash.
     */
    public static CustomerResponse toResponse(Customer entity) {
        return CustomerResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .kycStatus(entity.getKycStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
