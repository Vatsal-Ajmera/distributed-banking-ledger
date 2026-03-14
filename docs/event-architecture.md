# Event Driven Architecture

The system uses an event-driven architecture for asynchronous communication between services.

Events are published to Kafka topics.
Services subscribe to topics relevant to their domain.

This approach improves scalability and decouples services.

---

# Why Use Event Messaging

Benefits:

* loose coupling between services
* improved scalability
* asynchronous processing
* resilience to service failures

---

# Event Broker

Kafka is used as the message broker.

Each important domain event is published to a Kafka topic.

Services consume events from topics to trigger actions.

---

# Core Topics

Topic: transfer-events

Contains events related to transfer lifecycle.

Events:

transfer_initiated
transfer_completed
transfer_failed

---

Topic: account-events

Contains events related to account changes.

Events:

account_created
account_debited
account_credited

---

Topic: notification-events

Used to trigger user notifications.

Events:

transfer_notification
deposit_notification
withdrawal_notification

---

Topic: audit-events

Used for compliance logging.

Events:

login_attempt
transfer_created
transfer_completed
access_denied

---

# Example Transfer Event

Event published by Transfer Service:

{
"eventType": "transfer_initiated",
"transferId": "T12345",
"fromAccountId": "A001",
"toAccountId": "A002",
"amount": 500,
"timestamp": "2026-03-15T10:22:00Z"
}

---

# Event Consumers

Transfer Service

Consumes:

account_debited
account_credited

---

Notification Service

Consumes:

transfer_completed
transfer_failed

Sends notifications to users.

---

Audit Service

Consumes all major events.

Stores them in an immutable audit log.

---

# Event Flow Example

Client initiates transfer

Transfer Service publishes:

transfer_initiated

↓

Account Service processes debit and publishes:

account_debited

↓

Account Service processes credit and publishes:

account_credited

↓

Transfer Service publishes:

transfer_completed

↓

Notification Service sends alert to user
