# Smart Inventory Management System

A command-line Java application for managing product inventory, tracking stock movements, generating analytics reports, and exporting data — built with clean architecture and production-quality reliability practices.

---

## Problem Statement

Small and medium businesses often rely on spreadsheets or manual processes to manage inventory. These approaches are error-prone, offer no audit trail, and provide no real-time insight into stock health. This project demonstrates how a well-structured Java console application can address these problems by providing:

- A persistent product catalog with duplicate prevention.
- Audited purchase and sale operations with immutable transaction records.
- Real-time analytics (dashboard, sales reports, best-selling product detection).
- Data export in RFC 4180-compliant CSV format.
- Robust input validation and safe file persistence across all operations.

---

## Project Objectives

1. Model real-world inventory entities (products, transactions) with strict validation.
2. Persist data reliably across sessions using atomic file writes.
3. Track every stock movement with an auditable transaction history.
4. Provide an analytics layer: dashboard metrics, sales summaries, and best-seller detection.
5. Export inventory and transaction data to CSV for external reporting tools.
6. Defend against invalid input, overselling, integer overflow, and partial persistence failures.
7. Demonstrate clean, layered Java architecture without external frameworks or build tools.

---

## Features

### Product Management (Phase 1)
- **Add product** — Requires a unique ID, name, category, positive price, non-negative quantity, and non-negative low-stock threshold. Duplicate IDs (case-insensitive) are rejected.
- **View all products** — Lists every product with its current stock value.
- **Search product by ID** — Case-insensitive lookup.
- **Update product** — Replaces all mutable fields; product ID is immutable. Invalid values are rejected before any state is changed.
- **Delete product** — Removes a product by ID; silently returns false for unknown IDs.
- **Sort products by price** — Returns products ordered from lowest to highest price.

### Low-Stock Detection (Phase 2)
- **Low-stock alerts** — Lists all products whose current quantity is at or below their configured threshold.
- **Total inventory value** — Sums `price × quantity` across all products.

### Stock Transactions (Phase 3)
- **Purchase stock** — Increases a product's quantity; records an immutable `PURCHASE` transaction with a unique `TXN-NNNNN` ID, timestamp, unit price, and total amount. Detects integer overflow before modifying any state.
- **Sell stock** — Decreases a product's quantity; rejects oversell attempts before any state is changed. Records an immutable `SALE` transaction.
- **Transaction receipt** — Printed to the console after every successful purchase or sale.
- **View transaction history** — Displays all transactions sorted by timestamp, then transaction ID.

### File Persistence (Phase 4)
- Products are stored in `data/products.txt`; transactions in `data/transactions.txt`.
- Both files use UTF-8 tab-separated records. String fields (ID, name, category) are Base64-encoded to safely handle any Unicode text including tabs and newlines.
- Writes use a write-to-temp-then-atomic-move strategy: partial writes never corrupt the live data file.
- `saveAll()` writes transactions first, then products. If the product write fails, the transaction file is rolled back to its previous content.
- Malformed lines on load raise an `IOException` with the line number and reason; the application refuses to start with a corrupt data file.
- Duplicate IDs detected during load are also reported as errors.

### Inventory Reports and Analytics (Phase 5)

#### Inventory Dashboard
Displays a real-time summary of the entire catalog:

| Metric | Description |
|---|---|
| Total products | Count of distinct products in the catalog |
| Total stock quantity | Sum of all product quantities |
| Total inventory value | Sum of `price × quantity` for every product (via `BigDecimal`) |
| Low-stock products | Count of products at or below their threshold |

#### Sales and Purchase Report
Aggregates all recorded transactions:

| Metric | Description |
|---|---|
| Total sales value | Sum of all `SALE` transaction amounts |
| Total purchase value | Sum of all `PURCHASE` transaction amounts |
| Sales count | Number of sale transactions |
| Purchase count | Number of purchase transactions |
| Units sold | Total units across all sales |
| Units purchased | Total units across all purchases |
| Best-selling product | Product with the highest total units sold; revenue used as a tiebreaker |

#### CSV Export
- **Export inventory to CSV** — Writes `Product ID, Name, Category, Price, Quantity, Low Stock Threshold, Low Stock Alert, Stock Value` to a chosen file (default: `reports/inventory.csv`).
- **Export transactions to CSV** — Writes `Transaction ID, Timestamp, Product ID, Transaction Type, Quantity, Unit Price, Total Amount` (default: `reports/transactions.csv`).
- Both exports follow RFC 4180: fields containing commas, double-quotes, or line breaks are quoted; embedded double-quotes are doubled.
- Exports use temp-file-then-atomic-move, matching the persistence strategy used for data files.
- Re-exporting to an existing file cleanly replaces its content.
- Overwriting the active `products.txt` or `transactions.txt` storage files is explicitly rejected.

### Validation and Reliability (Phase 6)

#### Input Validation
- All console input loops until valid data is provided; empty strings are never accepted for required fields.
- Integer and double inputs that cannot be parsed are re-prompted with a descriptive error.
- Prices must be finite and positive; quantities and thresholds must be non-negative.
- Selling more stock than is available is caught at both the UI layer and the service layer.

#### Service-Layer Invariants
- Operations that fail validation leave in-memory state and persisted files completely unchanged — there are no partial updates.
- `purchaseStock` uses `Math.addExact` to detect integer overflow before applying any change.
- `updateProduct` validates the replacement `Product` object (via the `Product` constructor) before persisting; a failed validation does not touch the stored record.
- `findProductById` and `findTransactionById` return `Optional.empty()` for `null` or blank inputs rather than throwing.
- `deleteProduct` returns `false` for `null` or blank inputs rather than throwing.

#### Storage Reliability
- Temp files are always deleted in `finally` blocks regardless of success or failure.
- No orphaned `.tmp` files are left in the data directory after any operation.
- `CsvUtil.formatMonetary` safely handles `NaN`, `±Infinity`, and `null BigDecimal` inputs, returning `"0.00"` instead of propagating exceptions into export output.
- `InventoryDashboard` constructor rejects negative metrics and a low-stock count that exceeds the total product count.

---

## Architecture

```
SmartInventoryManagementSystem/
├── src/
│   └── com/koushik/inventory/
│       ├── app/
│       │   └── Main.java              # Console UI, menu loop, input helpers
│       ├── model/
│       │   ├── Product.java           # Validated product entity (immutable ID)
│       │   ├── Transaction.java       # Immutable transaction record
│       │   ├── TransactionType.java   # Enum: PURCHASE | SALE
│       │   ├── InventoryDashboard.java    # Snapshot of real-time inventory metrics
│       │   ├── SalesAndPurchaseReport.java  # Aggregated transaction analytics
│       │   └── BestSellingProduct.java # Best-selling product summary
│       ├── service/
│       │   └── InventoryService.java  # Business logic, validation, analytics, CSV export
│       ├── storage/
│       │   └── FileStorage.java       # UTF-8 tab-separated persistence with atomic writes
│       └── util/
│           └── CsvUtil.java           # RFC 4180 CSV escaping and row formatting
├── test/
│   └── com/koushik/inventory/
│       ├── InventoryPersistenceTest.java           # Phase 4: persistence round-trip tests
│       ├── InventoryReportsTest.java               # Phase 5: dashboard and report tests
│       └── InventoryValidationReliabilityTest.java # Phase 6: validation and edge-case tests
├── data/
│   ├── products.txt       # Live product storage (auto-created on first run)
│   └── transactions.txt   # Live transaction storage (auto-created on first run)
└── reports/               # CSV export output directory (auto-created on export)
```

**Layer responsibilities:**

| Layer | Responsibility |
|---|---|
| `app` | Console I/O only — no business logic |
| `model` | Pure data holders with constructor-level validation |
| `service` | All business rules, analytics computation, and CSV export |
| `storage` | File I/O and serialization — no business rules |
| `util` | Stateless formatting helpers |

---

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21 or later (tested with JDK 27).
- No build tool, no external dependencies — only the JDK is required.

### Compile

Run from the project root:

```powershell
# Windows PowerShell
javac -d out (Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName)
```

```bash
# Linux / macOS
find src -name "*.java" | xargs javac -d out
```

### Run

```powershell
# Windows PowerShell
java -cp out com.koushik.inventory.app.Main
```

```bash
# Linux / macOS
java -cp out com.koushik.inventory.app.Main
```

The application creates the `data/` directory automatically on first run.

### Run Tests

```powershell
# Compile sources and tests together, then run all three test suites
javac -d out (Get-ChildItem -Recurse -Filter *.java src,test | Select-Object -ExpandProperty FullName)
java -cp out com.koushik.inventory.InventoryPersistenceTest
java -cp out com.koushik.inventory.InventoryReportsTest
java -cp out com.koushik.inventory.InventoryValidationReliabilityTest
```

All three suites print a single success message on pass and throw `AssertionError` with a descriptive label on failure. No third-party test framework is required.

---

## Data File Format

### `data/products.txt`

One product per line, six tab-separated fields:

```
<Base64 productId>  <Base64 name>  <Base64 category>  <price>  <quantity>  <threshold>
```

String fields are Base64-encoded so that any Unicode character, including tabs and newlines, can be stored safely.

### `data/transactions.txt`

One transaction per line, seven tab-separated fields:

```
<Base64 txnId>  <Base64 productId>  <PURCHASE|SALE>  <quantity>  <unitPrice>  <totalAmount>  <ISO-8601 timestamp>
```

### CSV Export Format

Inventory export (`reports/inventory.csv`) — eight columns:

```
Product ID,Name,Category,Price,Quantity,Low Stock Threshold,Low Stock Alert,Stock Value
```

Transaction export (`reports/transactions.csv`) — seven columns:

```
Transaction ID,Timestamp,Product ID,Transaction Type,Quantity,Unit Price,Total Amount
```

Both files use UTF-8 encoding, CRLF line endings, and RFC 4180 field escaping.

---

## Application Menu

```
---------- MAIN MENU ----------
 1.  Add Product
 2.  View All Products
 3.  Search Product
 4.  Update Product
 5.  Delete Product
 6.  Sort Products by Price
 7.  Low Stock Alerts
 8.  Total Inventory Value
 9.  Purchase Stock
10.  Sell Stock
11.  View Transaction History
12.  Inventory Dashboard
13.  Sales and Purchase Reports
14.  Export Inventory to CSV
15.  Export Transactions to CSV
 0.  Exit
------------------------------
```

---

## Development Phases

| Phase | Description | Git Commit |
|---|---|---|
| Phase 1 | Core product management (add, view, search, update, delete, sort) | `3fffd36` |
| Phase 2 | Low-stock detection and inventory valuation | `3fffd36` |
| Phase 3 | Stock purchases, sales, and transaction history | `87028f0` |
| Phase 4 | File persistence with atomic writes and Base64 encoding | `886aa77` |
| Phase 5 | Inventory dashboard, sales/purchase reports, and CSV export | `9ed1a82` |
| Phase 6 | Input validation, overflow protection, and persistence reliability | `142949b` |
| Phase 7 | Professional documentation (this README) | — |

---

## Key Design Decisions

- **No build tool** — The project compiles with a single `javac` invocation to keep the setup friction-free.
- **No third-party libraries** — All functionality, including CSV escaping and `BigDecimal` arithmetic, uses the Java standard library.
- **`BigDecimal` for monetary values** — Transaction totals and dashboard valuations use `BigDecimal` to avoid floating-point rounding errors.
- **Base64 storage encoding** — Avoids the need for delimiter escaping in the internal data files; any product name or ID can be stored without format ambiguity.
- **Atomic file writes** — Every save operation writes to a temporary file first and then moves it into place, preventing data loss from partial writes.
- **Immutable transaction records** — Transactions are never modified after creation, providing a reliable audit trail.
- **Fail-before-mutate discipline** — All validation in `InventoryService` is completed before any in-memory or on-disk state is changed, ensuring failed operations leave the system in a consistent state.

---

## License

This project is part of the [JavaRealWorldSystems](https://github.com/koushik8369/JavaRealWorldSystems) repository and is intended for educational and portfolio purposes.
