package com.bank.customer.controller;

import com.bank.customer.dto.CustomerRegistrationRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.dto.CustomerUpdateRequest;
import com.bank.customer.entity.KycStatus;
import com.bank.customer.exception.CustomerNotFoundException;
import com.bank.customer.exception.DuplicateEmailException;
import com.bank.customer.exception.GlobalExceptionHandler;
import com.bank.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for {@link CustomerController}.
 * Uses {@code @WebMvcTest} — no database, service is mocked.
 * SecurityConfig is excluded; only GlobalExceptionHandler is imported
 * so error-response shaping is also verified.
 */
@WebMvcTest(controllers = CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    // ---- shared fixture ----

    private static final UUID CUSTOMER_ID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String BASE_PATH   = "/api/v1/customers";
    private static final String VALID_EMAIL = "alice@example.com";
    private static final String VALID_NAME  = "Alice";
    private static final String VALID_PASS  = "s3cur3P@ss";

    private CustomerResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = CustomerResponse.builder()
                .id(CUSTOMER_ID)
                .name(VALID_NAME)
                .email(VALID_EMAIL)
                .kycStatus(KycStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    // ================================================================
    // POST /api/v1/customers — register
    // ================================================================
    @Nested
    @DisplayName("POST /api/v1/customers")
    class Register {

        private CustomerRegistrationRequest validRequest() {
            return CustomerRegistrationRequest.builder()
                    .name(VALID_NAME)
                    .email(VALID_EMAIL)
                    .password(VALID_PASS)
                    .build();
        }

        @Test
        @DisplayName("should return 201 CREATED with response body on success")
        void register_success_returns201() throws Exception {
            when(customerService.register(any())).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                    .andExpect(jsonPath("$.kycStatus").value("PENDING"));
        }

        @Test
        @DisplayName("should accept optional X-Idempotency-Key header without error")
        void register_withIdempotencyKey_returns201() throws Exception {
            when(customerService.register(any())).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should return 201 when X-Idempotency-Key header is absent (header is optional)")
        void register_withoutIdempotencyKey_returns201() throws Exception {
            when(customerService.register(any())).thenReturn(sampleResponse);

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should return 409 CONFLICT when email already exists")
        void register_duplicateEmail_returns409() throws Exception {
            when(customerService.register(any())).thenThrow(
                    new DuplicateEmailException("A customer with this email already exists"));

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when name is blank")
        void register_blankName_returns400() throws Exception {
            CustomerRegistrationRequest bad = CustomerRegistrationRequest.builder()
                    .name("")
                    .email(VALID_EMAIL)
                    .password(VALID_PASS)
                    .build();

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("name")));
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when email is malformed")
        void register_invalidEmail_returns400() throws Exception {
            CustomerRegistrationRequest bad = CustomerRegistrationRequest.builder()
                    .name(VALID_NAME)
                    .email("not-an-email")
                    .password(VALID_PASS)
                    .build();

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when password is too short")
        void register_shortPassword_returns400() throws Exception {
            CustomerRegistrationRequest bad = CustomerRegistrationRequest.builder()
                    .name(VALID_NAME)
                    .email(VALID_EMAIL)
                    .password("short")
                    .build();

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================================================
    // GET /api/v1/customers/{id}
    // ================================================================
    @Nested
    @DisplayName("GET /api/v1/customers/{id}")
    class GetById {

        @Test
        @DisplayName("should return 200 OK with customer data")
        void getById_success_returns200() throws Exception {
            when(customerService.getById(CUSTOMER_ID)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_PATH + "/" + CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.email").value(VALID_EMAIL));
        }

        @Test
        @DisplayName("should return 404 NOT FOUND when customer does not exist")
        void getById_notFound_returns404() throws Exception {
            when(customerService.getById(CUSTOMER_ID))
                    .thenThrow(new CustomerNotFoundException("No customer found with id " + CUSTOMER_ID));

            mockMvc.perform(get(BASE_PATH + "/" + CUSTOMER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    // ================================================================
    // GET /api/v1/customers?email=...
    // ================================================================
    @Nested
    @DisplayName("GET /api/v1/customers?email=")
    class GetByEmail {

        @Test
        @DisplayName("should return 200 OK with customer data")
        void getByEmail_success_returns200() throws Exception {
            when(customerService.getByEmail(VALID_EMAIL)).thenReturn(sampleResponse);

            mockMvc.perform(get(BASE_PATH).param("email", VALID_EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(VALID_EMAIL));
        }

        @Test
        @DisplayName("should return 404 NOT FOUND when email is unknown")
        void getByEmail_notFound_returns404() throws Exception {
            when(customerService.getByEmail(VALID_EMAIL))
                    .thenThrow(new CustomerNotFoundException("No customer found"));

            mockMvc.perform(get(BASE_PATH).param("email", VALID_EMAIL))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // PUT /api/v1/customers/{id}
    // ================================================================
    @Nested
    @DisplayName("PUT /api/v1/customers/{id}")
    class Update {

        @Test
        @DisplayName("should return 200 OK with updated data")
        void update_success_returns200() throws Exception {
            CustomerResponse updated = CustomerResponse.builder()
                    .id(CUSTOMER_ID)
                    .name("Alice Updated")
                    .email(VALID_EMAIL)
                    .kycStatus(KycStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();

            when(customerService.update(eq(CUSTOMER_ID), any())).thenReturn(updated);

            CustomerUpdateRequest req = CustomerUpdateRequest.builder().name("Alice Updated").build();

            mockMvc.perform(put(BASE_PATH + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Alice Updated"));
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when name is blank")
        void update_blankName_returns400() throws Exception {
            CustomerUpdateRequest bad = CustomerUpdateRequest.builder().name("").build();

            mockMvc.perform(put(BASE_PATH + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 NOT FOUND when customer does not exist")
        void update_notFound_returns404() throws Exception {
            when(customerService.update(eq(CUSTOMER_ID), any()))
                    .thenThrow(new CustomerNotFoundException("Not found"));

            CustomerUpdateRequest req = CustomerUpdateRequest.builder().name("Alice").build();

            mockMvc.perform(put(BASE_PATH + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================================================
    // DELETE /api/v1/customers/{id}
    // ================================================================
    @Nested
    @DisplayName("DELETE /api/v1/customers/{id}")
    class Delete {

        @Test
        @DisplayName("should return 204 NO CONTENT on successful soft-delete")
        void delete_success_returns204() throws Exception {
            doNothing().when(customerService).delete(CUSTOMER_ID);

            mockMvc.perform(delete(BASE_PATH + "/" + CUSTOMER_ID))
                    .andExpect(status().isNoContent());

            verify(customerService).delete(CUSTOMER_ID);
        }

        @Test
        @DisplayName("should return 204 NO CONTENT even when customer already deleted (idempotent)")
        void delete_alreadyDeleted_returns204() throws Exception {
            // Service treats delete as idempotent — never throws
            doNothing().when(customerService).delete(CUSTOMER_ID);

            mockMvc.perform(delete(BASE_PATH + "/" + CUSTOMER_ID))
                    .andExpect(status().isNoContent());
        }
    }
}
