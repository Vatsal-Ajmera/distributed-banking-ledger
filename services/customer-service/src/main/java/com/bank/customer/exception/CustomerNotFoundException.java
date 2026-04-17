package com.bank.customer.exception;

/**
 * Thrown when a customer cannot be found by ID or email.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
