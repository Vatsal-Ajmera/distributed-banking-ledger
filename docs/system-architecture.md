# System Architecture

The Distributed Core Banking Ledger system is built using a microservices architecture.
Each service owns a specific business domain and maintains its own database.

Services communicate through REST APIs and asynchronous events.

---

# Services Overview

## API Gateway

The API Gateway acts as the single entry point for all client requests.

Responsibilities:

* request routing
* authentication
* rate limiting
* request validation

Clients never communicate directly with internal services.

---

## Customer Service

Responsible for managing customer information.

Responsibilities:

* customer registration
* authentication
* profile management

Owned data:

* Customer

---

## Account Service

Responsible for bank accounts and balances.

Responsibilities:

* account creation
* balance queries
* deposit operations
* withdrawal operations

Owned data:

* Account
* LedgerEntry
* LedgerTransaction

---

## Transfer Service

Responsible for transfers between accounts.

Responsibilities:

* initiate transfer
* validate transfer request
* coordinate distributed transaction
* maintain transfer status

Owned data:

* Transfer

---

## Notification Service

Handles communication with users.

Responsibilities:

* send transfer notifications
* send transaction alerts
* process event notifications

Consumes events from other services.

---

## Audit Service

Maintains immutable audit logs for compliance.

Responsibilities:

* record important system events
* track transaction activity
* provide audit trail

---

# Communication Model

Services communicate using two mechanisms.

## REST APIs

Used for synchronous operations.

Examples:

* create customer
* create account
* query balance

---

## Event Driven Messaging

Used for asynchronous workflows.

Example events:

* transfer_initiated
* transfer_completed
* transfer_failed
* account_created

Events are published to Kafka topics and consumed by interested services.

---

# High Level Flow Example

Transfer Workflow:

1. Client sends transfer request to API Gateway
2. Gateway routes request to Transfer Service
3. Transfer Service validates accounts
4. Transfer Service requests Account Service to debit sender
5. Account Service creates ledger entries
6. Transfer Service requests credit to receiver
7. Transfer is marked completed
8. Notification Service sends alert
