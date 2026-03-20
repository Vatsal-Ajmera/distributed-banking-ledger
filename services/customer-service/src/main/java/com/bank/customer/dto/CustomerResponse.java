package com.bank.customer.dto;

import com.bank.customer.entity.KycStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private KycStatus kycStatus;
    private boolean active;
    private Instant createdAt;
}
