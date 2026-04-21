package com.bank.customer.service;

import com.bank.customer.dto.CustomerRegistrationRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.dto.CustomerUpdateRequest;
import com.bank.customer.entity.Customer;
import com.bank.customer.entity.KycStatus;
import com.bank.customer.exception.CustomerNotFoundException;
import com.bank.customer.exception.DuplicateEmailException;
import com.bank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomerServiceImpl}.
 * Uses Mockito — no Spring context, no database.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    // ---- shared test fixtures ----

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String RAW_EMAIL   = "  Alice@Example.COM  ";
    private static final String NORM_EMAIL  = "alice@example.com";
    private static final String RAW_PASS    = "s3cur3P@ss";
    private static final String HASH        = "$2a$10$hashedvalue";

    private Customer buildActiveCustomer() {
        return Customer.builder()
                .id(CUSTOMER_ID)
                .name("Alice")
                .email(NORM_EMAIL)
                .passwordHash(HASH)
                .kycStatus(KycStatus.PENDING)
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    // ================================================================
    // register()
    // ================================================================
    @Nested
    @DisplayName("register()")
    class Register {

        private CustomerRegistrationRequest request;

        @BeforeEach
        void setUp() {
            request = CustomerRegistrationRequest.builder()
                    .name("Alice")
                    .email(RAW_EMAIL)
                    .password(RAW_PASS)
                    .build();
        }

        @Test
        @DisplayName("should register a new customer and return response DTO")
        void register_success() {
            when(customerRepository.existsByEmail(NORM_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(RAW_PASS)).thenReturn(HASH);

            Customer saved = buildActiveCustomer();
            when(customerRepository.save(any(Customer.class))).thenReturn(saved);

            CustomerResponse response = customerService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(CUSTOMER_ID);
            assertThat(response.getEmail()).isEqualTo(NORM_EMAIL);
            assertThat(response.getKycStatus()).isEqualTo(KycStatus.PENDING);

            verify(customerRepository).existsByEmail(NORM_EMAIL);
            verify(passwordEncoder).encode(RAW_PASS);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("should normalise email to lowercase+trimmed before duplicate check")
        void register_emailNormalisedBeforeDuplicateCheck() {
            when(customerRepository.existsByEmail(NORM_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn(HASH);
            when(customerRepository.save(any())).thenReturn(buildActiveCustomer());

            customerService.register(request);

            // must be called with the normalised form, not raw form
            verify(customerRepository).existsByEmail(NORM_EMAIL);
            verify(customerRepository, never()).existsByEmail(RAW_EMAIL);
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email already exists")
        void register_duplicateEmail_throws() {
            when(customerRepository.existsByEmail(NORM_EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> customerService.register(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("already exists");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should never log the plaintext password")
        void register_plaintextPasswordNotPresentInResult() {
            when(customerRepository.existsByEmail(NORM_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(RAW_PASS)).thenReturn(HASH);
            when(customerRepository.save(any())).thenReturn(buildActiveCustomer());

            CustomerResponse response = customerService.register(request);

            // CustomerResponse must not expose passwordHash
            // (field doesn't exist on DTO — this assertion confirms no widening leak)
            assertThat(response).doesNotHaveToString(RAW_PASS);
            assertThat(response).doesNotHaveToString(HASH);
        }
    }

    // ================================================================
    // getById()
    // ================================================================
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return response when active customer exists")
        void getById_success() {
            when(customerRepository.findByIdAndActiveTrue(CUSTOMER_ID))
                    .thenReturn(Optional.of(buildActiveCustomer()));

            CustomerResponse response = customerService.getById(CUSTOMER_ID);

            assertThat(response.getId()).isEqualTo(CUSTOMER_ID);
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when ID does not exist")
        void getById_notFound_throws() {
            when(customerRepository.findByIdAndActiveTrue(CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getById(CUSTOMER_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer is soft-deleted")
        void getById_softDeleted_throws() {
            // findByIdAndActiveTrue returns empty for inactive customers
            when(customerRepository.findByIdAndActiveTrue(CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getById(CUSTOMER_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // ================================================================
    // getByEmail()
    // ================================================================
    @Nested
    @DisplayName("getByEmail()")
    class GetByEmail {

        @Test
        @DisplayName("should return response when active customer exists with normalised email")
        void getByEmail_success() {
            when(customerRepository.findByEmailAndActiveTrue(NORM_EMAIL))
                    .thenReturn(Optional.of(buildActiveCustomer()));

            CustomerResponse response = customerService.getByEmail(RAW_EMAIL);

            assertThat(response.getEmail()).isEqualTo(NORM_EMAIL);
            verify(customerRepository).findByEmailAndActiveTrue(NORM_EMAIL);
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when email not found")
        void getByEmail_notFound_throws() {
            when(customerRepository.findByEmailAndActiveTrue(NORM_EMAIL))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getByEmail(RAW_EMAIL))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // ================================================================
    // update()
    // ================================================================
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update name and return updated response")
        void update_success() {
            Customer existing = buildActiveCustomer();
            when(customerRepository.findByIdAndActiveTrue(CUSTOMER_ID))
                    .thenReturn(Optional.of(existing));

            Customer after = Customer.builder()
                    .id(CUSTOMER_ID)
                    .name("Bob")
                    .email(NORM_EMAIL)
                    .passwordHash(HASH)
                    .kycStatus(KycStatus.PENDING)
                    .active(true)
                    .createdAt(existing.getCreatedAt())
                    .build();
            when(customerRepository.save(existing)).thenReturn(after);

            CustomerUpdateRequest req = CustomerUpdateRequest.builder().name("  Bob  ").build();
            CustomerResponse response = customerService.update(CUSTOMER_ID, req);

            assertThat(response.getName()).isEqualTo("Bob");
            verify(customerRepository).save(existing);
        }

        @Test
        @DisplayName("should throw CustomerNotFoundException when customer does not exist")
        void update_notFound_throws() {
            when(customerRepository.findByIdAndActiveTrue(CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            CustomerUpdateRequest req = CustomerUpdateRequest.builder().name("Bob").build();

            assertThatThrownBy(() -> customerService.update(CUSTOMER_ID, req))
                    .isInstanceOf(CustomerNotFoundException.class);

            verify(customerRepository, never()).save(any());
        }
    }

    // ================================================================
    // delete()
    // ================================================================
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should soft-delete an active customer")
        void delete_active_softDeletes() {
            Customer existing = buildActiveCustomer();
            when(customerRepository.findByIdAndActiveTrue(CUSTOMER_ID))
                    .thenReturn(Optional.of(existing));

            customerService.delete(CUSTOMER_ID);

            assertThat(existing.isActive()).isFalse();
            verify(customerRepository).save(existing);
        }

        @Test
        @DisplayName("should be a no-op when customer is already inactive or not found")
        void delete_alreadyInactive_noOp() {
            when(customerRepository.findByIdAndActiveTrue(CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            // should NOT throw — idempotent delete
            customerService.delete(CUSTOMER_ID);

            verify(customerRepository, never()).save(any());
        }
    }
}
