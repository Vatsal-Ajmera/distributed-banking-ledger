# Banking Domain Model

This document defines the core entities used in the Distributed Core Banking Ledger system.

---

# 1 Customer

Represents a bank customer who owns accounts.

Fields:

```
customer_id
name
email
password_hash
kyc_status
created_at
```

Relationships:

* One customer can own multiple accounts.

---

# 2 Account

Represents a bank account belonging to a customer.

Fields:

```
account_id
customer_id
account_type
currency
status
created_at
```

Example account types:

```
SAVINGS
CURRENT
```

Relationships:

* Each account belongs to one customer.
* Accounts participate in financial transactions.

---

# 3 Ledger Transaction

Represents a financial transaction recorded in the system.

Fields:

```
transaction_id
reference_id
transaction_type
status
created_at
```

Examples:

```
DEPOSIT
WITHDRAWAL
TRANSFER
```

Each transaction produces ledger entries.

---

# 4 Ledger Entry

Represents a single debit or credit in the accounting ledger.

Fields:

```
entry_id
transaction_id
account_id
debit_amount
credit_amount
created_at
```

Rules:

* Every transaction must produce at least two ledger entries.
* The total debit amount must equal the total credit amount.

This enforces double-entry accounting.

---

# 5 Transfer

Represents a money transfer between two accounts.

Fields:

```
transfer_id
from_account_id
to_account_id
amount
status
created_at
```

Status examples:

```
PENDING
COMPLETED
FAILED
CANCELLED
```

Transfers generate ledger transactions and entries.

Customer
|
| owns
|
Account
|
| participates in
|
LedgerEntry
|
| belongs to
|
LedgerTransaction
``