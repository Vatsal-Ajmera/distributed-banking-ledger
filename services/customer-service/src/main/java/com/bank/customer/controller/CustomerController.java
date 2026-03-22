package com.bank.customer.controller;

import com.bank.customer.dto.CustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public CustomerResponse createCustomer(
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey
    ) {
        log.info("Received request to create customer with email: {}", request.getEmail());
        return customerService.createCustomer(request, idempotencyKey);
    }

    @GetMapping("/{email}")
    public CustomerResponse getCustomerByEmail(@PathVariable String email) {
        log.info("Received request to fetch customer with email: {}", email);
        return customerService.getCustomerByEmail(email);
    }
}
