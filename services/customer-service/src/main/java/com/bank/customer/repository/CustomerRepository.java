package com.bank.customer.repository;

import com.bank.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Customer} entities.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Find an active customer by email.
     */
    Optional<Customer> findByEmailAndActiveTrue(String email);

    /**
     * Find an active customer by ID.
     */
    Optional<Customer> findByIdAndActiveTrue(UUID id);

    /**
     * Check if any customer (active or soft-deleted) exists with the given email.
     * Used for idempotent duplicate detection — email uniqueness is global.
     */
    boolean existsByEmail(String email);
}
