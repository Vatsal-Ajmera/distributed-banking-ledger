package com.bank.customer.repository;

import com.bank.customer.entity.Customer;
import com.bank.customer.entity.KycStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration tests using a real PostgreSQL container (Testcontainers).
 * Flyway migrations are applied automatically during context startup.
 *
 * <p>Annotated with {@code @DataJpaTest} so only the JPA slice is loaded —
 * no web layer, no security, no full Spring context.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("customer_db_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Flyway runs migrations from classpath:db/migration automatically
        registry.add("spring.flyway.enabled",      () -> "true");
    }

    @Autowired
    private CustomerRepository customerRepository;

    // ---- helpers ----

    private Customer buildAndSave(String email, boolean active) {
        Customer c = Customer.builder()
                .name("Test User")
                .email(email)
                .passwordHash("$2a$10$somehash")
                .kycStatus(KycStatus.PENDING)
                .active(active)
                .build();
        return customerRepository.save(c);
    }

    @BeforeEach
    void cleanUp() {
        customerRepository.deleteAll();
    }

    // ================================================================
    // findByEmailAndActiveTrue()
    // ================================================================
    @Nested
    @DisplayName("findByEmailAndActiveTrue()")
    class FindByEmailAndActiveTrue {

        @Test
        @DisplayName("should find an active customer by email")
        void findActive_returnsCustomer() {
            buildAndSave("alice@example.com", true);

            Optional<Customer> result = customerRepository.findByEmailAndActiveTrue("alice@example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
        }

        @Test
        @DisplayName("should not find a soft-deleted customer by email")
        void findActive_softDeleted_returnsEmpty() {
            buildAndSave("bob@example.com", false);

            Optional<Customer> result = customerRepository.findByEmailAndActiveTrue("bob@example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void findActive_unknownEmail_returnsEmpty() {
            Optional<Customer> result = customerRepository.findByEmailAndActiveTrue("nobody@example.com");
            assertThat(result).isEmpty();
        }
    }

    // ================================================================
    // findByIdAndActiveTrue()
    // ================================================================
    @Nested
    @DisplayName("findByIdAndActiveTrue()")
    class FindByIdAndActiveTrue {

        @Test
        @DisplayName("should find an active customer by ID")
        void findActive_returnsCustomer() {
            Customer saved = buildAndSave("carol@example.com", true);

            Optional<Customer> result = customerRepository.findByIdAndActiveTrue(saved.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("should not find a soft-deleted customer by ID")
        void findActive_softDeleted_returnsEmpty() {
            Customer saved = buildAndSave("dave@example.com", false);

            Optional<Customer> result = customerRepository.findByIdAndActiveTrue(saved.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty for a random unknown UUID")
        void findActive_unknownId_returnsEmpty() {
            Optional<Customer> result = customerRepository.findByIdAndActiveTrue(UUID.randomUUID());
            assertThat(result).isEmpty();
        }
    }

    // ================================================================
    // existsByEmail()
    // ================================================================
    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("should return true for an active customer's email")
        void existsByEmail_active_returnsTrue() {
            buildAndSave("eve@example.com", true);
            assertThat(customerRepository.existsByEmail("eve@example.com")).isTrue();
        }

        @Test
        @DisplayName("should return true even for a soft-deleted customer's email (global uniqueness)")
        void existsByEmail_softDeleted_stillReturnsTrue() {
            buildAndSave("frank@example.com", false);
            // email uniqueness is enforced globally — even deleted customers block re-registration
            assertThat(customerRepository.existsByEmail("frank@example.com")).isTrue();
        }

        @Test
        @DisplayName("should return false for an email that was never registered")
        void existsByEmail_unknown_returnsFalse() {
            assertThat(customerRepository.existsByEmail("ghost@example.com")).isFalse();
        }
    }

    // ================================================================
    // Soft-delete lifecycle
    // ================================================================
    @Nested
    @DisplayName("Soft-delete lifecycle")
    class SoftDeleteLifecycle {

        @Test
        @DisplayName("should persist active=false after soft-delete save")
        void softDelete_persistsActiveFlag() {
            Customer saved = buildAndSave("heidi@example.com", true);
            saved.setActive(false);
            customerRepository.save(saved);

            Customer reloaded = customerRepository.findById(saved.getId()).orElseThrow();
            assertThat(reloaded.isActive()).isFalse();
        }

        @Test
        @DisplayName("should set createdAt on initial persist")
        void save_setsCreatedAt() {
            Customer saved = buildAndSave("ivan@example.com", true);
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }
}
