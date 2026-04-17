CREATE TABLE customers (
                           id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           name          VARCHAR(100)  NOT NULL,
                           email         VARCHAR(255)  NOT NULL UNIQUE,
                           password_hash VARCHAR(255)  NOT NULL,
                           kyc_status    VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
                           active        BOOLEAN       NOT NULL DEFAULT TRUE,
                           created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
                           updated_at    TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_customer_email ON customers (email);
