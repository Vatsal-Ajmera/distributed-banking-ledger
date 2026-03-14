# Transfer Saga Workflow

The system uses the Saga Pattern to coordinate distributed transactions across services.

A transfer consists of multiple steps executed by different services.
If one step fails, compensating actions are executed.

---

# Transfer Flow

Step 1 — Client Request

Client sends request:

POST /transfers

Payload:

{
fromAccountId
toAccountId
amount
}

API Gateway authenticates the user and forwards the request to Transfer Service.

---

Step 2 — Transfer Initialization

Transfer Service performs validation:

* accounts exist
* accounts are active
* sufficient balance

Transfer record is created:

status = PENDING

---

Step 3 — Debit Source Account

Transfer Service calls Account Service:

POST /accounts/debit

Account Service:

* checks balance
* creates ledger entries
* places hold on funds

If successful → event published

event: account_debited

---

Step 4 — Credit Destination Account

Transfer Service receives debit confirmation.

Transfer Service calls:

POST /accounts/credit

Account Service:

* credits receiver account
* creates ledger entries

If successful → event published

event: account_credited

---

Step 5 — Transfer Completion

Transfer Service updates transfer:

status = COMPLETED

Notification Service sends confirmation to the user.

---

# Failure Handling

If credit operation fails:

Transfer Service triggers compensation.

Compensation step:

reverse debit operation.

Account Service removes hold and restores funds.

Transfer status becomes:

FAILED

---

# Saga Event Flow

transfer_initiated
↓
account_debited
↓
account_credited
↓
transfer_completed

Failure case:

transfer_initiated
↓
account_debited
↓
credit_failed
↓
debit_reversed
↓
transfer_failed
