package com.bank.customer.dto;

import com.bank.customer.entity.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound DTO for customer data.
 * Never exposes passwordHash.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private UUID id;
    private String name;
    private String email;
    private KycStatus kycStatus;
    private Instant createdAt;
}
