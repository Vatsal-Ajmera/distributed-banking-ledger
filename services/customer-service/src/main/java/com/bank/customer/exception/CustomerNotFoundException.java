package com.bank.customer.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String email) {
        super("Customer not found with email: " + email);
    }
}
