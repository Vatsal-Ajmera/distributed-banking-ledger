# Distributed Core Banking Ledger

## Overview

Distributed Core Banking Ledger is a microservices-based backend system that simulates the internal banking infrastructure responsible for managing customers, accounts, and financial transactions.

The system is designed to demonstrate production-grade backend engineering practices used in modern financial systems such as:

* distributed transaction management
* event-driven architecture
* double-entry ledger accounting
* microservice orchestration

The platform supports fundamental banking operations including account creation, deposits, withdrawals, and inter-account transfers.

---

## System Architecture

The system is built using a microservices architecture where each service is responsible for a specific domain.

Core services include:

* **API Gateway** – Entry point for client requests and authentication
* **Customer Service** – Manages customer registration and profiles
* **Account Service** – Maintains bank accounts and ledger balances
* **Transfer Service** – Handles inter-account fund transfers
* **Notification Service** – Sends transaction alerts
* **Audit Service** – Maintains immutable audit logs for compliance

---

## Key Engineering Concepts

This project demonstrates several important distributed systems concepts:

### Double Entry Ledger

All financial transactions follow double-entry accounting principles where every debit is matched with a corresponding credit.

### Saga Pattern

Money transfers across services use the Saga pattern to maintain consistency across distributed services.

### Event Driven Architecture

Services communicate through asynchronous events using Kafka.

### Idempotency

Transfers are protected against duplicate execution using idempotent request handling.

---

## Technology Stack

Backend

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Security

Infrastructure

* PostgreSQL
* Apache Kafka
* Redis

Deployment

* Docker
* Kubernetes

Observability

* Prometheus
* Grafana
* OpenTelemetry

---

## Planned Features

* Customer registration and authentication
* Bank account creation
* Deposit and withdrawal operations
* Inter-account transfers
* Distributed transaction management
* Event-based notification system
* Audit logging
* Monitoring and tracing

---

## Repository Structure

```
core-banking-ledger
│
├── services
│   ├── api-gateway
│   ├── customer-service
│   ├── account-service
│   ├── transfer-service
│   ├── notification-service
│   └── audit-service
│
├── infrastructure
│   ├── docker
│   ├── postgres
│   └── kafka
│
├── diagrams
└── docs
```

---

## Project Goal

The objective of this project is to build a production-style backend system that demonstrates strong understanding of:

* financial system design
* distributed microservices
* fault tolerant transaction workflows
* scalable backend architecture
z