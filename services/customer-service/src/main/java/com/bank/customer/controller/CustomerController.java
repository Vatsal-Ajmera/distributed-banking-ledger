package com.bank.customer.controller;

import com.bank.customer.entity.Customer;
import com.bank.customer.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    @GetMapping("/{email}")
    public Optional<Customer> getCustomerByEmail(@PathVariable String email) {
        return customerService.getCustomerByEmail(email);
    }
}
