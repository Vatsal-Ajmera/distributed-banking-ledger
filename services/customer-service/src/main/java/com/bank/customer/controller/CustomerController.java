package com.bank.customer.controller;

import com.bank.customer.dto.CustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public CustomerResponse createCustomer(
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey
    ) {
        return customerService.createCustomer(request, idempotencyKey);
    }


    @GetMapping("/{email}")
    public CustomerResponse getCustomerByEmail(@PathVariable String email) {
        return customerService.getCustomerByEmail(email);
    }
}
